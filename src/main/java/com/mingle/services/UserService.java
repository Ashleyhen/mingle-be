package com.mingle.services;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.entity.Audit;
import com.mingle.entity.MingleRoles;
import com.mingle.entity.MingleUser;
import com.mingle.exception.DuplicateException;
import com.mingle.exception.KeyCloakException;
import com.mingle.exception.MingleAuthenticationException;
import com.mingle.exception.NotFoundException;
import com.mingle.impl.IMingleCreate;
import com.mingle.repository.MingleUserRepository;
import com.mingle.utility.ValidateParams;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;


import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.security.Principal;
import java.util.*;

import static com.mingle.utility.ValidateParams.mingleFieldValidation;


@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService implements IMingleCreate<MingleUserDto> {

    @ConfigProperty(name="mingle.target.keycloak.realm")
    String targetRealm;

    private final MingleUserRepository mingleUserRepository;
    private final Keycloak keycloak;

    @Inject
    private CurrentIdentityAssociation currentIdentityAssociation;

    @WithTransaction
    public Uni<MingleUserDto> login(CredentialsDto credentials) {
           return currentIdentityAssociation.getDeferredIdentity()
                .onItem().transformToUni(securityIdentity -> {
                    if(securityIdentity.isAnonymous()){
                        return Uni.createFrom()
                                .failure(new MingleAuthenticationException("missing security identity")) ;
                    }
                       Principal principal = securityIdentity.getPrincipal();
                       System.out.println("Authenticated User: " + principal.getName());
                       return findUserByEmail(securityIdentity.getAttribute("email"))
                               .map(MingleUser::toMingleUserDto);
               });
    }



    @WithTransaction
    public Uni<MingleUserDto> update(MingleUserDto mingleUserDto) {
        return validateUpdateParams(mingleUserDto)
                .chain(() -> checkForEmailDuplicate(mingleUserDto))
                .chain(() -> checkUserExists(mingleUserDto))
                .chain((mingleUser) -> updateExistingUser(mingleUserDto,mingleUser));
    }


    @WithTransaction
    public Uni<MingleUserDto> create(MingleUserDto mingleUserDto) {
        return validateParams(mingleUserDto)
                .chain(() -> checkForDuplicates(mingleUserDto))
                .chain(() -> createNewMingleEntity(mingleUserDto));
    }

    private Uni<MingleUser> findUserByEmail(String email) {
        return mingleUserRepository.findByEmail(email)
                .onItem().ifNull().failWith(() ->
                        new MingleAuthenticationException("UNAUTHORIZED - User not found for email: "+ email));
    }





    public Uni<MingleUserDto> createNewMingleEntity(MingleUserDto mingleUserDto) {
        MingleUser newUser = new MingleUser(mingleUserDto);
        newUser.setIsActive(false); // Set user as inactive
        newUser.setAudit(Audit.builder().createdBy(mingleUserDto.getUsername()).build());
        String authUserId=saveToAuthServer(mingleUserDto);
        return newUser.persist()
                .onItem().castTo(MingleUser.class)
                .map(savedUser -> {
                    log.info("User successfully created: ID={}", savedUser.id);
                    return savedUser.toMingleUserDto();
                }).onFailure().invoke(()->{
                    try{
                        keycloak.realm(targetRealm).users().delete(authUserId);
                    } catch (Exception e) {
                        log.error("mismatch between keycloak and mingle db. " +
                                "Failed to delete user from keycloak and failed to save user in mingle db");
                        throw e;
                    }
                });
    }

    private String saveToAuthServer(MingleUserDto mingleUserDto) {
        // Create user representation (without roles initially)
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(mingleUserDto.getUsername());
        userRepresentation.setEmail(mingleUserDto.getEmail());
        userRepresentation.setFirstName(mingleUserDto.getFirstname());
        userRepresentation.setLastName(mingleUserDto.getLastname());
        userRepresentation.setEnabled(true);

        // Set credentials
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(mingleUserDto.getPassword());
        credentialRepresentation.setTemporary(false);
        userRepresentation.setCredentials(List.of(credentialRepresentation));

        // Create user
        Response response = keycloak.realm(targetRealm).users().create(userRepresentation);

        if (response.getStatus() != 201) {
            throw new KeyCloakException.UserCreationFailed(response.readEntity(String.class),response.getStatus());
        }
           String userId =CreatedResponseUtil.getCreatedId(response);
            // 4. Assign realm role (mingle-admin)
            RoleRepresentation role = keycloak.realm(targetRealm)
                .roles()
                .get(MingleRoles.mingle_admin.name())
                .toRepresentation();

            keycloak.realm(targetRealm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(List.of(role));
            return userId;
    }

    public Uni<MingleUserDto> validateParams(MingleUserDto mingleUserDto) {
        return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                    ValidateParams.email(mingleUserDto.getEmail()),
                    ValidateParams.firstName(mingleUserDto.getFirstname()),
                    ValidateParams.lastName(mingleUserDto.getLastname()),
                    ValidateParams.password(mingleUserDto.getPassword()),
                    ValidateParams.phone(mingleUserDto.getPhone()),
                    ValidateParams.zip(mingleUserDto.getZip()),
                    ValidateParams.mingleDateFormat(mingleUserDto.getBirthday()),
                    ValidateParams.gender(mingleUserDto.getGender())
            );
            return mingleUserDto; // Validation pass: no errors
        });
    }

    public Uni<MingleUserDto> validateUpdateParams(MingleUserDto mingleUserDto) {
        return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                    ValidateParams.email(mingleUserDto.getEmail()),
                    ValidateParams.firstName(mingleUserDto.getFirstname()),
                    ValidateParams.lastName(mingleUserDto.getLastname()),
                    ValidateParams.phone(mingleUserDto.getPhone()),
                    ValidateParams.zip(mingleUserDto.getZip()),
                    ValidateParams.mingleDateFormat(mingleUserDto.getBirthday()),
                    ValidateParams.gender(mingleUserDto.getGender())
            );
            return mingleUserDto; // Validation pass: no errors
        });
    }

    @Override
    public Uni<MingleUser> checkForDuplicates(MingleUserDto mingleUserDto) {
        return mingleUserRepository.findByEmailOrUsername(mingleUserDto.getEmail(), mingleUserDto.getUsername())
                .onItem().ifNotNull().failWith(() ->
                        new DuplicateException(
                                "Failed to create user!",
                                "Email or Username already exits",
                                mingleUserDto
                        ));
    }
    private Uni<MingleUser> checkUserExists(MingleUserDto mingleUserDto) {
        return mingleUserRepository.findById(mingleUserDto.getId())
                .onItem().ifNull().failWith(new NotFoundException(
                    "Failed to update user",
                    "Cannot update a user that doesn't exist",
                    "userId: " + mingleUserDto.getId() + " doesn't exist"
            ));
    }

    private Uni<Void> checkForEmailDuplicate(MingleUserDto mingleUserDto) {
        return mingleUserRepository.findByEmailOrUsername(mingleUserDto.getEmail(),mingleUserDto.getUsername())
                .onItem().castTo(MingleUser.class)
                .onItem().ifNotNull()
                .invoke(duplicateUser -> {
                    if (!Objects.equals(duplicateUser.id, mingleUserDto.getId())) {
                        throw new DuplicateException("Failed to update user","Can't update user to an existing username or email",mingleUserDto);
                    }
                }).replaceWithVoid(); // Return Void explicitly
    }

    private Uni<MingleUserDto> updateExistingUser(MingleUserDto mingleUserDto,MingleUser mingleUser) {
        return mingleUser.updateMingleUser(mingleUserDto)
                .persist()
                .onItem().castTo(MingleUser.class)
                .map(MingleUser::toMingleUserDto);
    }

}
