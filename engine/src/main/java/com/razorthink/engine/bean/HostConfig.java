package com.razorthink.engine.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HostConfig {
    private List<String> Binds;
    private List<String> Links;
    private long Memory;
    private long MemorySwap;
    private long MemoryReservation;
    private Integer CpuPercent;
    private Integer CpuShares;
    private Integer CpuPeriod;
    private Integer CpuQuota;
    private long NanoCpus;
    private Map<String, List<PortType> > PortBindings;

}