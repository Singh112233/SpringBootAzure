package com.springboot.azureapp.exception;

import com.springboot.azureapp.model.ErrorHandlerVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DemoModelNotFoundException.class)
    public ResponseEntity<ErrorHandlerVO> modelNotFoundErrorHandler(DemoModelNotFoundException demoModelNotFoundException, WebRequest webRequest){
     ErrorHandlerVO errorHandlerVO = new ErrorHandlerVO(HttpStatus.NOT_FOUND,demoModelNotFoundException.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorHandlerVO);
    }
}
