package com.mingle.exception;


import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ExceptionUtil {
    public static StatusRuntimeException exceptionHandler(Throwable failure) {
        if(failure instanceof MingleException){
            return ((MingleException) failure).toStatusRunTimeException();
        }
        log.error("UNKNOWN ERROR",failure);
        return new MingleException.UnknownException(failure).toStatusRunTimeException();
    }
}
