package com.razorthink.engine.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DeleteDTO {
    String containerName ;

    int infraType;

    String hostIp;

    String hostPort;

}
