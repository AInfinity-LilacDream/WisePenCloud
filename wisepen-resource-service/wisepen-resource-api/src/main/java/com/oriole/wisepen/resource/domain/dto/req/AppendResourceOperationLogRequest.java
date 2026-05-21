package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.enums.ResourceOperationDomain;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 追加一条资源操作流水（内部 Feign / 资源服务内部写入）。
 */
@Data
public class AppendResourceOperationLogRequest {

    /** 资源全局 ID；尚未注册资源时可为 null，应在 detail 中携带业务主键 */
    private String resourceId;

    private ResourceOperationDomain domain;

    /** 域内操作码，如 CREATE、DELETE、RENAME */
    private String operationType;

    private Long userId;

    /** 为空时由服务端写入为当前时间 */
    private LocalDateTime operationTime;

    private String resourceName;

    private String detail;
}
