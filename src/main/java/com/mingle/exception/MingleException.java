package com.mingle.exception;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@AllArgsConstructor
@Slf4j
public abstract class MingleException extends RuntimeException{
    public final ErrorDetail errorDetails;
    public final String debugDetail;
    public MingleException(String title, String description, String debugDetail){
        this.errorDetails = new ErrorDetail(title,description);
        this.debugDetail=debugDetail;
    }

    protected abstract Metadata getMetaData();
    protected abstract Status getStatus();

     public StatusRuntimeException toStatusRunTimeException() {
        log.error(this.debugDetail);
        return this.getStatus()
                .withDescription(Base64.getEncoder()
                        .encodeToString(this.errorDetails.toErrorDetailsResponse().toByteArray()))
                .asRuntimeException(this.getMetaData());
    }

    public static class UnknownException extends MingleException{

        public UnknownException( Throwable throwable) {
            super("Unknown error occurred", "", throwable.getMessage());
        }

        @Override
        protected Metadata getMetaData() {
            Metadata metadata= new Metadata();
            Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(httpStatusKey, "500"); // bad request
            return metadata;
        }

        @Override
        protected Status getStatus() {
            return Status.UNKNOWN;
        }
    }

}
