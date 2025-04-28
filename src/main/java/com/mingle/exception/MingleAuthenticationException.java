package com.mingle.exception;

import io.grpc.Metadata;
import io.grpc.Status;


public class MingleAuthenticationException extends MingleException{

    public MingleAuthenticationException(String debugMessage){
        super("UNAUTHENTICATED","Use a different email or password",debugMessage);
    }


    @Override
    protected Metadata getMetaData() {
        Metadata metadata= new Metadata();
        Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(httpStatusKey, "401"); // bad request
        return metadata;
    }
    @Override
    protected Status getStatus() {
        return Status.UNAUTHENTICATED;
    }
}
