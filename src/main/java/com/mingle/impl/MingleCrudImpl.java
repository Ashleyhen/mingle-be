package com.mingle.impl;

import com.google.protobuf.GeneratedMessageV3;
import com.mingle.exception.ExceptionUtil;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MingleCrudImpl<D extends GeneratedMessageV3> {

    private final IMingleCreate<D> iMingleCreate;

    public <T extends GeneratedMessageV3> Uni<T> create(D dto) {
        return iMingleCreate.validateParams(dto)
                .chain(() -> iMingleCreate.checkForDuplicates(dto))
                .chain(() -> iMingleCreate.createNewMingleEntity(dto))
                .onFailure()
                .transform(ExceptionUtil::exceptionHandler)
                .map(result -> (T) result); // Explicit cast to bounded type
    }
}
