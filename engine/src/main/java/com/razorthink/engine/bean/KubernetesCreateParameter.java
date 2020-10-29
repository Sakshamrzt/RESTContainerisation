package com.razorthink.engine.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class KubernetesCreateParameter implements Parameter {

    String containerName;

    String deploymentName;

    String image;

    String cpu;

    String memory;

}