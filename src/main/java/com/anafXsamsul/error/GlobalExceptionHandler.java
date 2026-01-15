package com.anafXsamsul.error;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.error.custom.BusinessException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.error("Error bussiness");
        return ResponseEntity.badRequest().body(
            ApiResponse.<Void>builder()
                .statusCode(400)
                .message(ex.getMessage())
                .data(null)
            .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {

        log.error("Error validasi input : " + ex.getMessage());

        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
        .orElse("Other error");

        return ResponseEntity.badRequest().body(
            ApiResponse.<Void>builder()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .data(null)    
            .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex ) {
        log.error("Error proses data");

        String message = "Data sedang diproses oleh sistem";

        if (ex.getMostSpecificCause().getMessage().contains("username")) {
            message = "Username sudah terdaftar";
        } else if (ex.getMostSpecificCause().getMessage().contains("email")) {
            message = "Email sudah terdaftar";
        }

        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .data(null)
        .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource() {
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Other error : " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.<Void>builder()
            .statusCode(500)
            .message("Other Error")
            .build()
        );
    }
    
}
