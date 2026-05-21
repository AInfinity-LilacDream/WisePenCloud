package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.ResourceOperationDomain;
import com.oriole.wisepen.resource.enums.ResourceOperationType;
import com.oriole.wisepen.resource.validation.ResourceOperationTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 追加一条资源操作流水。由 resource service 进行写入。
 */
@Data
public class AppendResourceOperationLogRequest {

    /** 资源全局 ID；尚未注册资源时可为空，应在 detail 中携带业务主键 */
    private String resourceId;

    @NotNull(message = ResourceValidationMsg.RESOURCE_OPERATION_DOMAIN_NOT_NULL)
    private ResourceOperationDomain domain;

    /** 域内操作短码，须为 {@link ResourceOperationType} 之一 */
    @NotBlank(message = ResourceValidationMsg.RESOURCE_OPERATION_TYPE_NOT_BLANK)
    @ResourceOperationTypeCode
    private String operationType;

    private Long userId;

    /** 为空时由服务端写入为当前时间 */
    private LocalDateTime operationTime;

    private String resourceName;

    private String detail;
}
