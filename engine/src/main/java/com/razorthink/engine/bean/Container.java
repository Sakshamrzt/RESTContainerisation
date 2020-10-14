package com.razorthink.engine.bean;
import lombok.Data;

import java.util.UUID;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity
@NoArgsConstructor
public class Container {

    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy="org.hibernate.id.UUIDGenerator")
    @Column(nullable=false,updatable=false)
    @Type(type="uuid-char")
    private UUID id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private Integer containerizationPlatform;//0 for docker swarm, 1 for kubernetes and 2 for GCE(Add to swagger)

    Boolean isActive;

}