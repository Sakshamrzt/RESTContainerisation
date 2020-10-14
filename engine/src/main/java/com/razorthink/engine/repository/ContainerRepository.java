package com.razorthink.engine.repository;

import com.razorthink.engine.bean.Container;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

@Repository
public interface ContainerRepository extends CrudRepository<Container, UUID>{

}
