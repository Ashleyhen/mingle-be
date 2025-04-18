package com.mingle.grpc;


import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.UserGrpc;
import com.mingle.exception.MingleUserException;
import com.mingle.services.UserService;
import io.grpc.Metadata;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@GrpcService
@Slf4j
public class UserGrpcService implements UserGrpc {

    @Inject
    UserService userService;

    @Override
    @WithTransaction
    public Uni<MingleUserDto> login(CredentialsDto request) {
        return userService.authenticateWithEmailAndPassword(request)
                .onFailure().invoke(failure -> log.error("Exception stack trace: ", failure))
                .onFailure(UnauthorizedException.class)
                .transform(failure -> { // Transform the UnauthorizedException to a gRPC StatusRuntimeException
                    failure.getStackTrace();
                    // Create metadata
                    Metadata metadata = new Metadata();
                    Metadata.Key<String> debugInfoKey = Metadata.Key.of("debug-info", Metadata.ASCII_STRING_MARSHALLER);

                    // Add information to metadata
                    metadata.put(debugInfoKey, "Authentication attempt failed due to invalid credentials");
                    Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
                    metadata.put(httpStatusKey, "401"); // Add HTTP status code as metadata

                    return Status.UNAUTHENTICATED
                            .withDescription("Error: "+failure.getMessage())
                            .asRuntimeException(metadata);
                });
    }


    @Override
    @WithTransaction
    public Uni<SuccessMsg> create(MingleUserDto request) {
        return userService.createUser(request)
                .onFailure().invoke(failure -> {
                    log.error("Exception ", failure);
                    failure.getStackTrace();
                })
                .onFailure(MingleUserException.class)
                .transform(failure -> {
                    Metadata metadata = new Metadata();
                    // metadata keys
                    Metadata.Key<String> debugInfoKey = Metadata.Key.of("debug-info", Metadata.ASCII_STRING_MARSHALLER);
                    Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
                    metadata.put(httpStatusKey, "405"); // bad request

                    if(failure instanceof MingleUserException.MissingField){
                        metadata.put(debugInfoKey, "Missing required fields");
                        return Status.DATA_LOSS
                                .withDescription(failure.getMessage())
                                .asRuntimeException(metadata);
                    }
                    metadata.put(debugInfoKey, "User already exist duplicate user entry invalid");
                    return Status.ALREADY_EXISTS
                            .withDescription(failure.getMessage())
                            .asRuntimeException(metadata);
                });

    }

    @Override
    @WithTransaction
    public Uni<SuccessMsg> update(MingleUserDto request) {
        return userService.updateUser(request)
                .onFailure().invoke(failure -> {
                    log.error("Exception ", failure);
                    failure.getStackTrace();
                })
                .onFailure(MingleUserException.UserNotFound.class)
                .transform(failure -> {
                    Metadata metadata = new Metadata();
                    // metadata keys
                    Metadata.Key<String> debugInfoKey = Metadata.Key.of("debug-info", Metadata.ASCII_STRING_MARSHALLER);
                    Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
                    metadata.put(httpStatusKey, "400"); // bad request
                        metadata.put(debugInfoKey, ((MingleUserException.UserNotFound) failure).debugMessage);
                        return Status.INVALID_ARGUMENT
                                .withDescription(failure.getMessage())
                                .asRuntimeException(metadata);


                });

    }
}
