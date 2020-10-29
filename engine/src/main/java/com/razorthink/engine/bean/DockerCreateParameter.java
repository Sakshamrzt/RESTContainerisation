package com.razorthink.engine.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class DockerCreateParameter implements Parameter {

    private String Image;

    private HostConfig HostConfig;

}