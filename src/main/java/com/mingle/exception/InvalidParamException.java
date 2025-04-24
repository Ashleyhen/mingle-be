package com.mingle.exception;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


public  class InvalidParamException extends MingleException {
    public final List<String> errors;

    public InvalidParamException(String title,List<String> errors ){
        super(title,String.join("\n",errors),"Error: "+errors.stream()
                .map(t-> "Error: "+t ).collect(Collectors.joining()));
        this.errors =errors;
    }

    @Override
    protected Metadata getMetaData() {
        Metadata metadata= new Metadata();
        Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(httpStatusKey, "405"); // bad request
        return metadata;
    }

    @Override
    protected Status getStatus() {
        return Status.ALREADY_EXISTS;
    }
}



