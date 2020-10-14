package com.razorthink.engine.service;
import com.razorthink.engine.repository.ContainerRepository;
import org.springframework.stereotype.Service;
import com.razorthink.engine.bean.Container;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ContainerServiceImpl implements ContainerService {

    @Autowired
    ContainerRepository containerRepository;

    @Override
    public void createContainer(String containerName, Integer infraType)
    {
        Container container= new Container();
        container.setIsActive(Boolean.TRUE);
        container.setName(containerName);
        container.setContainerizationPlatform(infraType);
        //actual creation logic goes here
        containerRepository.save(container);
    }

}
