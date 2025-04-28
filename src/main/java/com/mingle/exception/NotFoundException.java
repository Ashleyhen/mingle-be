package com.mingle.exception;


import io.grpc.Metadata;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotFoundException extends  MingleException{


    public NotFoundException(String title, String description, String debugDetail) {
        super(title, description, debugDetail);
    }

    @Override
    protected Metadata getMetaData() {
        Metadata metadata= new Metadata();
        Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(httpStatusKey, "404"); // bad request
        return metadata;
    }

    @Override
    protected Status getStatus() {
        return Status.NOT_FOUND;
    }
}

