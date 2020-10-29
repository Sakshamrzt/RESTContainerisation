package com.razorthink.engine.service.impl;

import com.razorthink.engine.dao.Container;
import com.razorthink.engine.bean.*;
import com.razorthink.engine.exception.ResourceNotFoundException;
import com.razorthink.engine.dao.repository.ContainerRepository;
import com.razorthink.engine.service.ContainerService;
import com.razorthink.engine.util.CommonUtils;
import com.razorthink.engine.util.DockerUtil;
import com.razorthink.engine.util.KubernetesUtil;
import com.razorthink.engine.util.ParameterFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.razorthink.engine.dto.DeleteDTO;
import com.razorthink.engine.dto.UpdateDTO;
import com.razorthink.engine.dto.ContainerInput;

@Service
public class ContainerServiceImpl implements ContainerService {

    @Autowired
    ContainerRepository containerRepository;

    @Autowired
    ParameterFactory parameterFactory;

    CommonUtils commonUtils = new CommonUtils();

    KubernetesUtil kubernetesUtil= new KubernetesUtil();

    @Override
    public void createContainer(ContainerInput containerInput)
    {
        Parameter parameter=parameterFactory.createParameter(containerInput);

        DockerUtil dockerUtil = new DockerUtil(containerInput.getName());


        commonUtils.validateContainerInput(containerInput.getContainerizationPlatform(),containerInput.getName());

        if( containerInput.getContainerizationPlatform()==0 )//docker
        {
            dockerUtil.addContainer((DockerCreateParameter) parameter,containerInput.getHostIp(),containerInput.getHostPort());
            dockerUtil.startContainer(containerInput.getHostIp(),containerInput.getHostPort());
        }
        else //kubernetes
        {
            kubernetesUtil.createDeployment((KubernetesCreateParameter) parameter);
        }

        Container container= commonUtils.createContainerFromContainerInput(containerInput);

        containerRepository.save(container);
    }

    public void deleteContainer(DeleteDTO deleteDTO)
    {
        commonUtils.validateContainerInput(deleteDTO.getInfraType(), deleteDTO.getContainerName());

        DockerUtil dockerUtil = new DockerUtil(deleteDTO.getContainerName());

        Container container = containerRepository.findByNameAndContainerizationPlatform(deleteDTO.getContainerName(),deleteDTO.getInfraType());

        if( container ==null )
            throw  new ResourceNotFoundException("No container with the given name present for this infratype");

        if( deleteDTO.getInfraType()==0 )//docker
        {
            dockerUtil.deleteContainer(deleteDTO.getHostIp(),deleteDTO.getHostPort());
        }
        else //kubernetes
        {
            kubernetesUtil.deleteDeployment(deleteDTO.getContainerName());
        }

        containerRepository.delete(container);
    }

    public  void updateContainer( UpdateDTO updateDTO)
    {
        commonUtils.validateContainerInput(updateDTO.getInfraType(), updateDTO.getContainerName());

        DockerUtil dockerUtil = new DockerUtil(updateDTO.getContainerName());

        Container container = containerRepository.findByNameAndContainerizationPlatform(updateDTO.getContainerName(),updateDTO.getInfraType());

        if( container ==null )
            throw  new ResourceNotFoundException("No container with the given name present for this infratype");

        if( updateDTO.getInfraType()==0 )//docker
        {
            dockerUtil.updateContainer(updateDTO.getHostIp(),updateDTO.getHostPort(),commonUtils.createMemoryOrCpuParameterDocker(updateDTO.getMemory()),commonUtils.createMemoryOrCpuParameterDocker(updateDTO.getCpu()));
        }
        else //kubernetes
        {
            kubernetesUtil.updateResources(updateDTO.getContainerName(),"",updateDTO.getMemory().toString(),updateDTO.getMemory().toString());
        }
        container.setRam(updateDTO.getMemory());
        container.setCpu(updateDTO.getCpu());
        containerRepository.save(container);
    }
}