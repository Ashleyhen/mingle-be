package com.mingle.exception;


import lombok.extern.slf4j.Slf4j;

@Slf4j

public abstract class MingleUserException extends RuntimeException {

    public final String debugMessage;

    public MingleUserException(String message,String debugMessage) {
        super(message);
        this.debugMessage=debugMessage;
        log.error(message);
    }

    public MingleUserException(String message) {
        super(message);
        this.debugMessage="";
        log.error(message);
    }

    public static class DuplicateUser extends MingleUserException {
        public DuplicateUser(String user, String email) {
            super("duplicate user: "+user+" email: "+email+" already exist");
        }
    }
    public static class MissingField extends MingleUserException {
        public MissingField(String message) {
            super(message);
        }
    }

    public static class InvalidPassword extends MingleUserException {
        public InvalidPassword(String message) {
            super(message);
        }
    }

    public static class InvalidPhoneNumber extends MingleUserException {
        public InvalidPhoneNumber(String message) {
            super(message);
        }
    }
    public static class InvalidEmail extends MingleUserException {
        public InvalidEmail(String message) {
            super(message);
        }
    }

    public static class InvalidZip extends MingleUserException {
        public InvalidZip(String message) {
            super(message);
        }
    }
    public static class UserNotFound extends  MingleUserException{
        public UserNotFound(String message,String debugMessage){super(message,debugMessage);}
    }
}
