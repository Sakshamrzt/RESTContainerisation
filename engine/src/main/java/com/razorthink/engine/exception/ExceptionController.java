package com.razorthink.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.fasterxml.jackson.databind.JsonMappingException;

@ControllerAdvice
public class ExceptionController {

   @ExceptionHandler(value = ResourceNotFoundException.class)
   public ResponseEntity<Object> exception(ResourceNotFoundException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
   }

   @ExceptionHandler(value = BadRequestException.class)
   public ResponseEntity<Object> exception(BadRequestException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
   }

}