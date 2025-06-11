package com.mingle.repository;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;

public interface IFindWithUser<T extends PanacheEntity> {
    Uni<T> findByIdWithOrganizer(String sub);
}
