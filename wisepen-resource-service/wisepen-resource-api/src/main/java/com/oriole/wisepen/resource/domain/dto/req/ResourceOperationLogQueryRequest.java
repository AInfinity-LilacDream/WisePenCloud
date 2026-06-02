package com.oriole.wisepen.resource.domain.dto.req;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ResourceOperationLogQueryRequest {
    private String resourceId;
    private Long userId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
    private int page = 1;
    private int size = 20;
}
