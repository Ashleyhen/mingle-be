package com.mingle.impl;

import com.google.protobuf.GeneratedMessageV3;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;


public interface IMingleCreate<D extends GeneratedMessageV3> {

    Uni<D> validateParams(D dto);

    Uni<? extends PanacheEntity> checkForDuplicates(D mingleUserDto);

    Uni<? extends GeneratedMessageV3> createNewMingleEntity(D mingleUserDto) ;

}
