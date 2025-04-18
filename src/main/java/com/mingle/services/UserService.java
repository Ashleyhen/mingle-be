package com.mingle.services;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.entity.MingleUser;
import com.mingle.exception.MingleUserException;
import com.mingle.repository.MingleUserRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.runtime.util.StringUtil;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Inject
    MingleUserRepository mingleUserRepository;

    @WithTransaction
    public Uni<MingleUserDto> authenticateWithEmailAndPassword(CredentialsDto credentials) {
        log.info(String.valueOf(credentials));
        String invalidPwd="Authentication attempt failed due to invalid credentials";
        return mingleUserRepository.findByEmail(credentials.getEmail())
                .onItem()
                .ifNull().failWith(() ->
                {
                    log.error("UNAUTHORIZED - User not found");
                    return new UnauthorizedException(invalidPwd);
                }).onItem().transformToUni(user->{
                    if(BCrypt.verifyer().verify(credentials.getPassword().toCharArray(),user.getPassword()).verified){
                        log.info("found a user");
                        // Clear sensitive data and map entity to DTO
                        return Uni.createFrom().item(user.toMingleUserDto());
                    }
                    log.error("UNAUTHORIZED - Invalid credentials");
                    return Uni.createFrom().failure(new UnauthorizedException(invalidPwd));
                }) ;
    }

    @WithTransaction
    public Uni<SuccessMsg> createUser(MingleUserDto mingleUserDto) {
        return mingleUserRepository.findByEmailOrPassword(mingleUserDto.getEmail(),mingleUserDto.getPassword())
                .onItem().transformToUni(result -> {
                    if (result != null) {
                        log.error("Duplicate user found with email: {}", obscureEmail(mingleUserDto.getEmail()));
                        return Uni.createFrom().failure(
                                new MingleUserException.DuplicateUser(mingleUserDto.getUsername(), mingleUserDto.getEmail())
                        );
                    }

                    mingleFieldValidation(mingleUserDto);

                    MingleUser newUser = new MingleUser(mingleUserDto);
                    String hashedPassword = BCrypt.withDefaults().hashToString(12, mingleUserDto.getPassword().toCharArray());
                    newUser.setPassword(hashedPassword);
                    newUser.setIsActive(false); // Set user as inactive

                    return newUser.persist()
                            .map(t->(MingleUser)t)
                            .map(mingleUser -> {
                                log.info("User successfully created: ID={}", mingleUser.id);
                                return SuccessMsg.newBuilder()
                                        .setMessage(String.valueOf(mingleUser.id))
                                        .build();
                            });
                });
    }

    private String obscureEmail(String email) {
        int atIndex = email.indexOf('@');
        return email.substring(0, 2) + "****" + email.substring(atIndex);
    }
    public Uni<SuccessMsg> updateUser(MingleUserDto mingleUserDto){
        return mingleUserRepository.findById(mingleUserDto.getId()).onItem()
                .ifNull().failWith(()->{
                    String errorMsg="userId: "+mingleUserDto.getId() +" doesn't exist";
                    log.error(errorMsg);
                    throw new MingleUserException
                            .UserNotFound(
                                    errorMsg,
                                    "Cannot update a user that doesn't exist"
                            );
                }).onItem().transform((mingleUser->{
                    mingleUser.updateMingleUser(mingleUserDto).persist();
                    return SuccessMsg.newBuilder().setMessage("Mingle user updated successfully").build();
                }));
    }

    private void mingleFieldValidation(MingleUserDto mingleUserDto) {
        isNullValidation(mingleUserDto);
        isValidPassword(mingleUserDto.getPassword());
        isValidPhoneNumber(mingleUserDto.getPhone());
        isValidEmail(mingleUserDto.getEmail());
        isValidZip(Integer.parseInt(mingleUserDto.getZip()));
    }

    public static void isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";
        if (!password.matches(passwordPattern)) {
            throw new MingleUserException.InvalidPassword("""
                    Invalid password password must meet the requirements
                    1. More then 5 characters for more long
                    2. Includes uppercase and lowercase
                    3. At least one digit
                    """);
        }
        ;
    }
    public static void isValidZip(int zip) {
        // Check if ZIP is in the valid range (5-digit format)
        if (zip < 10000 || zip > 99999) {
            throw new MingleUserException.InvalidZip("""
                Invalid ZIP code format.
                ZIP must be a 5-digit number.
                Example: 12345
                """);
        }
    }


    public static void isValidEmail(String email) {
        // Regex for basic email validation
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        // Validate email
        if (!email.matches(emailPattern)) {
            throw new MingleUserException.InvalidEmail("""
                    Invalid email
                    Example format: someone@email.com
                    """);
        }

    }

    public static void isValidPhoneNumber(String phoneNumber) {
        // Regex for phone number validation:
        // 1. Optional "+" for international format
        // 2. Country code (1–3 digits)
        // 3. 10-digit number (with or without spaces, dashes, or parentheses)
        String phoneNumberPattern = "^(\\+\\d{1,3})?\\s?-?\\(?\\d{3}\\)?\\s?-?\\d{3}\\s?-?\\d{4}$";

        if (!phoneNumber.matches(phoneNumberPattern)) {
            throw new MingleUserException.InvalidPhoneNumber("""
                    Invalid phone number must be at 10 digits
                    Example format: +1 (123) 456-7890
                    """);
        }
        ;
    }


    private void isNullValidation(MingleUserDto mingleUser) {
        String errorsMsg = Map.of(
                        MingleUser.Fields.email, mingleUser.getEmail(),
                        MingleUser.Fields.firstname, mingleUser.getFirstname(),
                        MingleUser.Fields.lastname, mingleUser.getLastname(),
                        MingleUser.Fields.password, mingleUser.getPassword(),
                        MingleUser.Fields.phone, mingleUser.getPhone(),
                        MingleUser.Fields.zip, mingleUser.getZip(),
                        MingleUser.Fields.birthday, mingleUser.getBirthday(),
                        MingleUser.Fields.gender, mingleUser.getGender()
                ).entrySet().stream().filter(t -> StringUtil.isNullOrEmpty(t.getValue()))
                .map(t -> t.getKey() + " is required! ").collect(Collectors.joining());

        if (!StringUtil.isNullOrEmpty(errorsMsg)) {
            throw new MingleUserException.MissingField(errorsMsg);
        }
    }
}
