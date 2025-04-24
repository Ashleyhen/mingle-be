package com.mingle.exception;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import mingle.Errors.ErrorDetailResponse;

import java.util.Base64;

@RequiredArgsConstructor
@Data
public class ErrorDetail {
    private final String title;
    private final String description;

    public ErrorDetailResponse toErrorDetailsResponse(){
        return ErrorDetailResponse.newBuilder()
                .setTitle(title)
                .setDescription(description)
                .build();
    }


}
