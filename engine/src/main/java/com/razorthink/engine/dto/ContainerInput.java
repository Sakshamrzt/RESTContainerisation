package com.razorthink.engine.dto;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class ContainerInput {

    private String name;

    private Integer containerizationPlatform;

    private String imageName;

    private Double memory;

    private Double cpu;

    Map<String,String> portMapping;

    private String hostIp;

    private String hostPort;

}
