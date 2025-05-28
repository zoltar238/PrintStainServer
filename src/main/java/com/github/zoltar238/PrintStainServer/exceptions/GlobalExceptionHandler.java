package com.github.zoltar238.PrintStainServer.exceptions;

import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailConflict(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseBuilder.buildResponse(false,
                        ex.getMessage(),
                        ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserConflict(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBuilder.buildResponse(false,
                        ex.getMessage(),
                        ex.getMessage()));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Object> handleUserConflict(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBuilder.buildResponse(false,
                        ex.getMessage(),
                        ex.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Object> handleUsernameConflict(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseBuilder.buildResponse(false,
                        ex.getMessage(),
                        ex.getMessage()));
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<Object> handleUnexpectedErrors(UnexpectedException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseBuilder.buildResponse(false, ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Object> handleItemNotFound(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBuilder.buildResponse(false, ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(CostOrPriceInvalidException.class)
    public ResponseEntity<Object> handleCostOrPriceInvalid(CostOrPriceInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBuilder.buildResponse(false, ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<Object> handleImageProcessingErrors(ImageProcessingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseBuilder.buildResponse(true, ex.getMessage(), ex.getMessage()));
    }
}
