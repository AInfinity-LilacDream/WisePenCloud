package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.enums.ResourceOperationDomain;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源操作流水单条记录（查询接口返回）。
 */
@Data
public class ResourceOperationLogResponse {

    private String id;
    private String resourceId;
    private ResourceOperationDomain domain;
    private String operationType;
    private Long userId;
    private LocalDateTime operationTime;
    private String resourceName;
    private String detail;
}
