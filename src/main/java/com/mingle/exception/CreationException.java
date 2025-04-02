package com.mingle.exception;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class  CreationException extends RuntimeException {
    public CreationException(String message) {
        super(message);
        log.error(message);
    }
    public static class DuplicateUser extends CreationException {
        public DuplicateUser(String message) {
            super(message);
        }
        public DuplicateUser(String user, String email) {
            super("duplicate user: "+user+" email: "+email+" already exist");
        }
    }
    public static class MissingField extends CreationException {
        public MissingField(String message) {
            super(message);
        }
    }

    public static class InvalidPassword extends CreationException {
        public InvalidPassword(String message) {
            super(message);
        }
    }

    public static class InvalidPhoneNumber extends CreationException {
        public InvalidPhoneNumber(String message) {
            super(message);
        }
    }
    public static class InvalidEmail extends CreationException {
        public InvalidEmail(String message) {
            super(message);
        }
    }
}
