package com.mingle.services;


import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.entity.MingleUser;
import com.mingle.exception.CreationException;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.runtime.util.StringUtil;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final ModelMapper modelMapper = new ModelMapper();

    @WithTransaction
    public Uni<MingleUserDto> authenticateWithEmailAndPassword(CredentialsDto credentials) {
        log.info(String.valueOf(credentials));
        return MingleUser.find("email = ?1 and password = ?2", credentials.getEmail(), credentials.getPassword())
                .firstResult() // Returns a Uni<MingleUser>
                .onItem()
                .ifNull().failWith(() ->
                {
                    log.error("UNAUTHORIZED - Invalid credentials");
                    return new UnauthorizedException("Authentication attempt failed due to invalid credentials");
                }).map(user -> {
                    log.info("found a user");
                    // Clear sensitive data and map entity to DTO
                    ((MingleUser) user).setPassword(null);
                    return modelMapper.map(user, MingleUserDto.class);
                });
    }

    @WithTransaction
    public Uni<Long> createUser(MingleUserDto mingleUserDto) {
        return MingleUser.find(
                        "email =?1 or username =?2", mingleUserDto.getEmail(), mingleUserDto.getUsername()
                )
                .firstResult() // Get the first matching user or null if none found
                .onItem()
                .transformToUni(result -> {
                    var mingleUser = (MingleUser) result;
                    if (result != null) {
                        log.error("Duplicate user found: userId={}, username={}, email={}",
                                mingleUser.id, mingleUser.getUsername(), mingleUser.getEmail());
                        throw new CreationException.DuplicateUser(mingleUser.getUsername(), mingleUser.getEmail());
                    }
                    isValidPassword(mingleUserDto.getPassword());
                    isValidPhoneNumber(mingleUserDto.getPhone());
                    isValidEmail(mingleUserDto.getEmail());

                    MingleUser newUser = modelMapper.map(validation(mingleUserDto), MingleUser.class);
                    newUser.setIsActive(false); // Set user as inactive
                    return newUser.persist().map(persisted -> newUser.id); //
                });
    }

    public static void isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";
        if (!password.matches(passwordPattern)) {
            throw new CreationException.InvalidPassword("""
                    Invalid password password must meet the requirements
                    1. More then 5 characters for more long
                    2. Includes uppercase and lowercase
                    3. At least one digit
                    """);
        }
        ;
    }

    public static void isValidEmail(String email) {
        // Regex for basic email validation
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        // Validate email
        if (email.matches(emailPattern)) {
            throw new CreationException.InvalidEmail("""
                    Invalid email
                    Example format: someone@email.com
                    """);
        }
        ;
    }

    public static void isValidPhoneNumber(String phoneNumber) {
        // Regex for phone number validation:
        // 1. Optional "+" for international format
        // 2. Country code (1–3 digits)
        // 3. 10-digit number (with or without spaces, dashes, or parentheses)
        String phoneNumberPattern = "^(\\+\\d{1,3})?\\s?-?\\(?\\d{3}\\)?\\s?-?\\d{3}\\s?-?\\d{4}$";

        if (phoneNumber.matches(phoneNumberPattern)) {
            throw new CreationException.InvalidPhoneNumber("""
                    Invalid phone number must be at 10 digits
                    Example format: +1 (123) 456-7890
                    """);
        }
        ;
    }


    private MingleUserDto validation(MingleUserDto mingleUser) {
        String errorsMsg = Map.of(
                        MingleUser.Fields.email, mingleUser.getEmail(),
                        MingleUser.Fields.firstname, mingleUser.getFirstname(),
                        MingleUser.Fields.lastname, mingleUser.getLastname(),
                        MingleUser.Fields.password, mingleUser.getPassword(),
                        MingleUser.Fields.phone, mingleUser.getPhone(),
                        MingleUser.Fields.zip, mingleUser.getZip(),
                        MingleUser.Fields.ageRange, mingleUser.getAgeRange(),
                        MingleUser.Fields.gender, mingleUser.getGender()
                ).entrySet().stream().filter(t -> StringUtil.isNullOrEmpty(t.getValue()))
                .map(t -> t.getKey() + " is required!").collect(Collectors.joining());

        if (StringUtil.isNullOrEmpty(errorsMsg)) {
            throw new CreationException.MissingField(errorsMsg);
        }
        return mingleUser;
    }
}
