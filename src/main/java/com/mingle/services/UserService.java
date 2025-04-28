package com.mingle.services;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.entity.Audit;
import com.mingle.entity.MingleUser;
import com.mingle.exception.DuplicateException;
import com.mingle.exception.MingleAuthenticationException;
import com.mingle.exception.NotFoundException;
import com.mingle.repository.MingleUserRepository;
import com.mingle.utility.ValidateParams;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.mingle.utility.ValidateParams.mingleFieldValidation;


@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService  {

    private final MingleUserRepository mingleUserRepository;

    @WithTransaction
    public Uni<MingleUserDto> login(CredentialsDto credentials) {
        log.info("Login attempt: {}", credentials);

        return findUserByEmail(credentials.getEmail())
                .chain(user -> verifyPassword(credentials.getPassword(), user))
                .map(MingleUser::toMingleUserDto);
    }



    @WithTransaction
    public Uni<SuccessMsg> update(MingleUserDto mingleUserDto) {
        return validateParams(mingleUserDto)
                .chain(() -> checkForEmailDuplicate(mingleUserDto))
                .chain(() -> checkUserExists(mingleUserDto))
                .chain((mingleUser) -> updateExistingUser(mingleUserDto,mingleUser));
    }


    @WithTransaction
    public Uni<SuccessMsg> create(MingleUserDto mingleUserDto) {
        return validateParams(mingleUserDto)
                .chain(() -> checkForDuplicateUser(mingleUserDto))
                .chain(() -> createNewUser(mingleUserDto));
    }

    private Uni<MingleUser> findUserByEmail(String email) {
        return mingleUserRepository.findByEmail(email)
                .onItem().ifNull().failWith(() ->
                        new MingleAuthenticationException("UNAUTHORIZED - User not found for email: "+ email));
    }

    private Uni<MingleUser> verifyPassword(String password, MingleUser user) {
        if (BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified) {
            log.info("Password verified for user: ID={}", user.id);
            return Uni.createFrom().item(user);
        }
        return Uni.createFrom().failure(new MingleAuthenticationException(
                "UNAUTHORIZED - Invalid password for user: ID="+ user.id) );
    }

    private Uni<MingleUser> checkForDuplicateUser(MingleUserDto mingleUserDto) {
        return mingleUserRepository.findByEmailOrUsername(mingleUserDto.getEmail(), mingleUserDto.getUsername())
                .onItem().ifNotNull().failWith(() ->
                        new DuplicateException(
                        "Failed to create user!",
                        "Email or Username already exits",
                        mingleUserDto
                ));
    }

    private Uni<SuccessMsg> createNewUser(MingleUserDto mingleUserDto) {
        MingleUser newUser = new MingleUser(mingleUserDto);
        String hashedPassword = BCrypt.withDefaults().hashToString(12, mingleUserDto.getPassword().toCharArray());
        newUser.setPassword(hashedPassword);
        newUser.setIsActive(false); // Set user as inactive
        newUser.setAudit(Audit.builder().createdBy(mingleUserDto.getUsername()).build());

        return newUser.persist()
                .onItem().castTo(MingleUser.class)
                .map(savedUser -> {
                    log.info("User successfully created: ID={}", savedUser.id);
                    return SuccessMsg.newBuilder().setMessage(String.valueOf(savedUser.id)).build();
                });
    }

    private static Uni<Void> validateParams(MingleUserDto mingleUserDto) {
        return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                    ValidateParams.email(mingleUserDto.getEmail()),
                    ValidateParams.firstName(mingleUserDto.getFirstname()),
                    ValidateParams.lastName(mingleUserDto.getLastname()),
                    ValidateParams.password(mingleUserDto.getPassword()),
                    ValidateParams.phone(mingleUserDto.getPhone()),
                    ValidateParams.zip(mingleUserDto.getZip()),
                    ValidateParams.birthday(mingleUserDto.getBirthday()),
                    ValidateParams.gender(mingleUserDto.getGender())
            );
            return null; // Validation pass: no errors
        });
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
                .invoke(duplicateUser -> {
                    if (!Objects.equals(duplicateUser.id, mingleUserDto.getId())) {
                        throw new DuplicateException("Failed to update user","Can't update user to an existing username or email",mingleUserDto);
                    }
                }).replaceWithVoid(); // Return Void explicitly
    }

    private Uni<SuccessMsg> updateExistingUser(MingleUserDto mingleUserDto,MingleUser mingleUser) {
        return mingleUser.updateMingleUser(mingleUserDto)
                .persist()
                .chain(() -> Uni.createFrom().item(SuccessMsg.newBuilder()
                                .setMessage("Mingle user updated successfully")
                                .build())
                        );
    }

}
