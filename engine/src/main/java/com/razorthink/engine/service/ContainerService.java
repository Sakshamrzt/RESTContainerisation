package com.razorthink.engine.service;

import com.razorthink.engine.dto.DeleteDTO;
import com.razorthink.engine.dto.UpdateDTO;
import com.razorthink.engine.dto.ContainerInput;

public interface  ContainerService {

    public  void createContainer( ContainerInput containerInput );

    public  void deleteContainer( DeleteDTO updateObject );

    public  void updateContainer( UpdateDTO deleteObject);

}
