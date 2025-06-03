package com.mingle.exception;

import io.grpc.Metadata;
import io.grpc.Status;

public class KeyCloakException extends MingleException {
    public KeyCloakException(String title,String detail,String debugMsg) {
        super(title,detail,debugMsg);
    }

    public static class UserCreationFailed extends  KeyCloakException{
      final int statusCode;
      public UserCreationFailed(String details, int statusCode) {
        super("User failed to save",details,"keycloak exception on save user "+ details);
        this.statusCode=statusCode;
      }

      @Override
      protected Metadata getMetaData() {
        Metadata metadata= new Metadata();
        Metadata.Key<String> httpStatusKey =
                Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(httpStatusKey, String.valueOf(statusCode)); // bad request
    return metadata;
      }
    }
  @Override
  protected Metadata getMetaData() {
    Metadata metadata= new Metadata();
    Metadata.Key<String> httpStatusKey =
            Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
    metadata.put(httpStatusKey, "401"); // bad request
    return metadata;
  }


  @Override
  protected Status getStatus() {
    return null;
  }
}
