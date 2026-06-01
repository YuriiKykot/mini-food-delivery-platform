package com.fooddelivery.orderservice.dto;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Builder.Default
    private String traceId = UUID.randomUUID().toString();

    private int status;
    private String message;
    private LocalDateTime time;
    private String path;
}
