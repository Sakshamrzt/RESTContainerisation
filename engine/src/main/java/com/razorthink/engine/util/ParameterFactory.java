package com.razorthink.engine.util;

import com.razorthink.engine.bean.*;
import com.razorthink.engine.dto.ContainerInput;
import org.springframework.stereotype.Component;


@Component
public class ParameterFactory {

    CommonUtils utils= new CommonUtils();

    public Parameter createParameter( ContainerInput containerInput )
    {
        switch ( containerInput.getContainerizationPlatform() )
        {
            case 0:
                HostConfig hostConfig=utils.createHostConfig(containerInput);
                return new DockerCreateParameter(containerInput.getImageName(),hostConfig);
            case 1:
                return new KubernetesCreateParameter(containerInput.getName(), containerInput.getName(), containerInput.getImageName(),
                        containerInput.getCpu().toString(), containerInput.getMemory().toString());
            default:
                return null;
        }
    }
}