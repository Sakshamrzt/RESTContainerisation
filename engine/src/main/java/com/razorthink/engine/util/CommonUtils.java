package com.razorthink.engine.util;

import com.razorthink.engine.dao.Container;
import com.razorthink.engine.dto.ContainerInput;
import com.razorthink.engine.bean.HostConfig;
import com.razorthink.engine.bean.PortType;
import com.razorthink.engine.exception.BadRequestException;
import java.util.regex.Pattern;

import java.util.*;

public class CommonUtils {
    public String createMemoryParameterKubernetes(Double memory)
    {
        String memoryValue=Double.toString(1024 * memory);
        memoryValue+="Mi";
        return memoryValue;
    }

    public HostConfig createHostConfig( ContainerInput containerInput)
    {
        HostConfig hostConfig= new HostConfig();
        hostConfig.setMemory(createMemoryOrCpuParameterDocker(containerInput.getMemory()));
        hostConfig.setNanoCpus(createMemoryOrCpuParameterDocker(containerInput.getMemory()));
        hostConfig.setPortBindings(formPortMapping(containerInput.getPortMapping()));
        return hostConfig;
    }

    public long createMemoryOrCpuParameterDocker(Double memoryOrCpu)
    {
        return (long) ((memoryOrCpu)*(Math.pow(10,9)));
    }

    public Map<String, List<PortType>> formPortMapping( Map<String, String> portMapping )
    {
        Map<String, List<PortType> > portBinding=new HashMap<String, List<PortType>>();

        for( String key : portMapping.keySet() )
        {
            PortType hostPort=new PortType();
            hostPort.setHostIp("0.0.0.0");
            hostPort.setHostPort(portMapping.get(key));
            List<PortType> portList;
            if(portBinding.containsKey(key))
            {
                portList=portBinding.get(key);
            }
            else
            {
                portList=new ArrayList<>();
            }
            portList.add(hostPort);
            portBinding.put(key+"/tcp",portList);
        }
        return portBinding;
    }

    public void validateContainerInput(int platform, String name)
    {
        List<Integer> infraType = new ArrayList<>();
        infraType.add(0);
        infraType.add(1);
        if(!infraType.contains(platform))
        {
            throw new BadRequestException("Wrong InfraType Given");
        }

        if(platform == 1  )
        {
            final String pattern = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";
            final String stringToCheck=name;
            final Pattern patternRegex = Pattern.compile(pattern);
            if (!patternRegex.matcher(stringToCheck).matches()) {
                throw new BadRequestException("Wrong Deployment Name ");
            }

        }

    }

    public Container createContainerFromContainerInput(ContainerInput containerInput)
    {
        Container container= new Container();
        container.setContainerizationPlatform(containerInput.getContainerizationPlatform());
        container.setName(containerInput.getName());
        container.setCpu(containerInput.getCpu());
        container.setRam(containerInput.getMemory());
        return container;
    }
}
