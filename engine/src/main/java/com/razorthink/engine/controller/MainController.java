package com.razorthink.engine.controller;

import com.razorthink.engine.bean.Container;
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

    @GetMapping("/addContainer")
    public ResponseEntity<Object> addContainer(@RequestParam String containerName, @RequestParam Integer infraType)
    {
        containerService.createContainer(containerName,infraType);
        return new ResponseEntity<>("Container was successfully added ", HttpStatus.CREATED);
    }

}
