package com.mingle.grpc;


import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.SuccessMsg;
import com.mingle.UserGrpc;
import com.mingle.exception.ExceptionUtil;
import com.mingle.impl.MingleCrudImpl;
import com.mingle.services.UserService;
import io.quarkus.grpc.GrpcService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@GrpcService
@Slf4j
@ActivateRequestContext
@RequiredArgsConstructor
public class MingleUserGrpc implements UserGrpc {

    private final UserService userService;


    @Inject
    private CurrentIdentityAssociation currentIdentityAssociation;

    private MingleCrudImpl<MingleUserDto> mingleCrud ;

    @PostConstruct
    void init(){
        mingleCrud= new MingleCrudImpl<>(userService);
    }

    @Override
    @WithTransaction
    public Uni<MingleUserDto> create(MingleUserDto request) {
        return  mingleCrud.create(request)
                .onItem().castTo(MingleUserDto.class)
                .onFailure().invoke(failure -> {
                    log.error("Exception ", failure);
                    failure.getStackTrace();
                })
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);

    }

    @Override
    @WithTransaction
    public Uni<MingleUserDto> login(CredentialsDto request) {
        currentIdentityAssociation.getDeferredIdentity()
                .onItem().transform(SecurityIdentity::getPrincipal)
                .subscribe().with(principal -> System.out.println("User: " + principal));
        return userService.login(request)
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);
    }

    @Override
    @WithTransaction
    public Uni<MingleUserDto> update(MingleUserDto request) {
        return userService.update(request)
                .onFailure().invoke(failure -> {
                    log.error("Exception ", failure);
                    failure.getStackTrace();
                })
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);

    }
}
