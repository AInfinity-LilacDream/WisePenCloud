package com.oriole.wisepen.resource.domain.dto.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceOperationLogResponse {
    private String logId;
    private String source;
    private String operationType;
    private LocalDateTime operationTime;
    private String resourceId;
    private String resourceName;
    private Long userId;
    private String contentSummary;
}
