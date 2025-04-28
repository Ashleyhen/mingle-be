package com.mingle.grpc;


import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.UserGrpc;
import com.mingle.exception.ExceptionUtil;
import com.mingle.services.UserService;
import io.quarkus.grpc.GrpcService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@GrpcService
@Slf4j
@RequiredArgsConstructor
public class MingleUserGrpc implements UserGrpc {

    private final UserService userService;

    @Override
    @WithTransaction
    public Uni<MingleUserDto> login(CredentialsDto request) {
        return userService.login(request)
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);
    }


    @Override
    @WithTransaction
    public Uni<SuccessMsg> create(MingleUserDto request) {
        return userService.create(request)
                .onFailure().invoke(failure -> {
                    log.error("Exception ", failure);
                    failure.getStackTrace();
                })
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);

    }



    @Override
    @WithTransaction
    public Uni<SuccessMsg> update(MingleUserDto request) {
        return userService.update(request)
                .onFailure().invoke(failure -> {
                    log.error("Exception ", failure);
                    failure.getStackTrace();
                })
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);

    }
}
