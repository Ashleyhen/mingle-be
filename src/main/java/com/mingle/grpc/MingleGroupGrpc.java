package com.mingle.grpc;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.mingle.GroupCreatedResponse;
import com.mingle.GroupGrpc;
import com.mingle.GroupUpdatedResponse;
import com.mingle.MingleGroupDto;
import com.mingle.exception.ExceptionUtil;
import com.mingle.exception.InvalidParamException;
import com.mingle.exception.MingleException;
import com.mingle.services.GroupService;
import io.grpc.Metadata;
import io.quarkus.grpc.ExceptionHandler;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.grpc.protobuf.StatusProto.toStatusRuntimeException;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class MingleGroupGrpc implements GroupGrpc {

    private final GroupService groupService;

    @Override
    public Uni<GroupCreatedResponse> createGroup(MingleGroupDto request) {
        return groupService.createGroup(request)
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler);
    }

    @Override
    public Uni<GroupUpdatedResponse> updateGroup(MingleGroupDto request) {
        return groupService.updateGroup(request)
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler); // Use centralized exception handler
    }

}