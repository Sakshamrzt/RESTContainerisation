package com.razorthink.engine.controller;

import com.razorthink.engine.dto.DeleteDTO;
import com.razorthink.engine.dto.UpdateDTO;
import com.razorthink.engine.dto.ContainerInput;
import com.razorthink.engine.service.ContainerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    ContainerService containerService;

    @PostMapping("/addContainer")
    public ResponseEntity<Object> addContainer(@RequestBody ContainerInput containerInput)
    {
        containerService.createContainer(containerInput);
        return new ResponseEntity<>("Container was successfully added ", HttpStatus.CREATED);
    }

    @DeleteMapping("/deleteContainer")
    public ResponseEntity<Object> deleteContainer(@RequestBody DeleteDTO deleteObject)
    {
        containerService.deleteContainer(deleteObject);
        return new ResponseEntity<>("Container was successfully deleted ", HttpStatus.ACCEPTED);
    }

    @PatchMapping("/updateResource")
    public ResponseEntity<Object> updateContainer(@RequestBody UpdateDTO updateObject )
    {
        containerService.updateContainer(updateObject);
        return new ResponseEntity<>("Container's resources were successfully updated ", HttpStatus.ACCEPTED);
    }

}

