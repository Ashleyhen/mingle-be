package com.mingle.grpc;

import com.mingle.*;
import com.mingle.entity.MingleLeague;
import com.mingle.exception.ExceptionUtil;
import com.mingle.impl.MingleCrudImpl;
import com.mingle.services.GroupService;
import com.mingle.services.LeagueService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class MingleGroupGrpc implements GroupGrpc {

    private final GroupService groupService;
    private MingleCrudImpl<MingleGroupDto> groupServiceImpl ;

    @PostConstruct
    void init(){
        groupServiceImpl= new MingleCrudImpl<>(groupService);
    }


    @Override
    public Uni<MingleGroupDto> createGroup(MingleGroupDto request) {
        return groupServiceImpl.create(request)
                .onItem()
                .castTo(MingleGroupDto.class);
    }

    @Override
    public Uni<MingleGroupDto> updateGroup(MingleGroupDto request) {
        return groupService.updateGroup(request)
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler); // Use centralized exception handler
    }

    @Override
    public Uni<ListMingleGroupDto> findAllGroupsByUserId(MingleId mingleUserId) {
        return groupService.findAllGroupsByUserId(mingleUserId.getId())
                .onFailure().transform(ExceptionUtil::exceptionHandler);
    }



}