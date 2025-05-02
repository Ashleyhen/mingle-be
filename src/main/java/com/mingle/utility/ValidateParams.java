package com.mingle.utility;

import com.mingle.exception.InvalidParamException;
import io.quarkus.runtime.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ValidateParams {
    private ValidateParams(){}

    public static Optional<String> zip(String zip) {
        // Check if the ZIP is null or does not match the 5-digit format
        if (zip == null || !zip.matches("\\d{5}")) {
            return Optional.of(
            """ 
            ZIP must be a 5-digit number.
            Example: 12345
            """);
        }
        // ZIP is valid
        return Optional.empty();
    }



    public static Optional<String> email(String email) {
        // Regex for basic email validation
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        // Validate email
        if (email == null ||!email.matches(emailPattern)) {
            return Optional.of("Invalid email someone@email.com");
        }
        return Optional.empty();

    }
    public static Optional<String> password(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";
        if (password ==null||!password.matches(passwordPattern)) {
            return Optional.of("""
                     Invalid password
                     password must meet the requirements
                    1. More then 5 characters for more long
                    2. Includes uppercase and lowercase
                    3. At least one digit
                    """);
        }
        return Optional.empty();
    }
    public static Optional<String> phone(String phoneNumber) {
        // Regex for phone number validation:
        // 1. Optional "+" for international format
        // 2. Country code (1–3 digits)
        // 3. 10-digit number (with or without spaces, dashes, or parentheses)
        String phoneNumberPattern = "^(\\+\\d{1,3})?\\s?-?\\(?\\d{3}\\)?\\s?-?\\d{3}\\s?-?\\d{4}$";

        if (phoneNumber ==null||!phoneNumber.matches(phoneNumberPattern)) {
            return Optional.of("Invalid phone number must be 10 digits");
        }
        return Optional.empty();
    }

    public static Optional<String> firstName(String firstName) { return basicValidation(firstName,"firstName"); }

    public static Optional<String> lastName(String lastName) {
        return basicValidation(lastName,"lastName");
    }
    public static Optional<String> groupName(String groupName) {
        return basicValidation(groupName,"groupName");
    }
    private static Optional<String> basicValidation(String field, String paramName) {
        return StringUtil.isNullOrEmpty(field)?
                Optional.of(paramName+" required"):
                Optional.empty();
    }

    public static Optional<String> gender(String gender) {
        if (StringUtil.isNullOrEmpty(gender) || !gender.matches("[FM]")){
            return Optional.of("Gender is required");
        }
        return Optional.empty();
    }

    public static Optional<String> mingleDateFormat(String date) {
        // Regular expression to match the format MM-DD-YYYY
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        // Check if the input matches the format
        if (date == null || !date.matches(regex)) {
            log.error("Invalid date format! date received {}",date);
            return Optional.of(
            """
            Invalid date format
            Date must be in the format YYYY-MM-DD.
            Example: 1995-11-24
            """);
        }
        // The input is valid
        return Optional.empty();
    }

    @SafeVarargs
    public static void mingleFieldValidation(Optional<String> ...errorList) {

        Set<String> errors = Stream.of(
                errorList
        ).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());

        if(!errors.isEmpty()){
            throw new InvalidParamException("Invalid Input",errors);
        }
    }

    public static Optional<String> eventName(String firstName) { return basicValidation(firstName,"event name"); }

}
