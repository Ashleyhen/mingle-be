package com.mingle.impl;

import com.google.protobuf.GeneratedMessageV3;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;


public interface IMingleCreate<D extends GeneratedMessageV3> {

    Uni<D> validateParams(D dto);

    Uni<? extends PanacheEntityBase> checkForDuplicates(D mingleUserDto);

    Uni<? extends GeneratedMessageV3> createNewMingleEntity(D mingleUserDto) ;

}
