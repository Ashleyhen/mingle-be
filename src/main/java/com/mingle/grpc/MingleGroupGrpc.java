package com.mingle.grpc;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.mingle.GroupCreatedResponse;
import com.mingle.GroupGrpc;
import com.mingle.GroupUpdatedResponse;
import com.mingle.MingleGroupDto;
import com.mingle.exception.InvalidParamException;
import com.mingle.exception.MingleException;
import com.mingle.services.GroupService;
import io.grpc.Metadata;
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
                .onFailure().transform(this::handleException); // Use centralized exception handler
    }

    @Override
    public Uni<GroupUpdatedResponse> updateGroup(MingleGroupDto request) {
        return groupService.updateGroup(request)
                .onFailure().transform(this::handleException); // Use centralized exception handler
    }

    private Throwable handleException(Throwable failure) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER), "400");
        log.error("Error: {}", failure.getMessage());

        if (failure instanceof MingleException) {
            return ((InvalidParamException) failure).toStatusRunTimeException();
        }

        log.error("Unexpected error occurred: {}", failure.getMessage());
        return toStatusRuntimeException(
                Status.newBuilder()
                .setMessage("An unexpected error occurred.")
                .setCode(Code.UNKNOWN_VALUE)
                .build()
        );
    }


}