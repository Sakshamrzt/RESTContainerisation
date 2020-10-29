package com.razorthink.engine.dao.repository;

import com.razorthink.engine.dao.Container;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContainerRepository extends CrudRepository<Container, UUID> {

    Container findByName( String name );

    Container findByNameAndContainerizationPlatform( String name, Integer containerizationPlatform );
}
