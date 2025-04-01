package com.mingle.grpc;


import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.UserGrpc;
import com.mingle.services.UserService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;


@GrpcService
public class UserGrpcService implements UserGrpc {

    @Inject
    UserService userService;

    @Override
    public Uni<MingleUserDto> login(CredentialsDto request) {
        return userService.AuthenticateWithEmailAndPassword(request);

    }

    @Override
    public Uni<SuccessMsg> create(MingleUserDto request) {
        return userService.createUser(request).onItem().transform(t -> SuccessMsg.newBuilder().setMessage(t.toString()).build());
    }
}
