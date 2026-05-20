package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.enums.ResourceOperationDomain;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 用户对文档 / 笔记相关资源的操作流水
 */
@Data
@Document(collection = ResourceOperationLogEntity.COLLECTION_NAME)
public class ResourceOperationLogEntity {

    public static final String COLLECTION_NAME = "wisepen_resource_operation_logs";

    @Id
    private String id;

    // 资源全局 ID 尚未注册资源时可为 null，应在 detail 中携带业务主键等
    private String resourceId;

    // 业务域：DOCUMENT 或 NOTE。
    private ResourceOperationDomain domain;

    // 域内操作码，例如 CREATE、DELETE、META_UPDATE
    @Field("operation_type")
    private String operationType;

    private Long userId;

    private LocalDateTime operationTime;

    // 操作资源名称快照
    private String resourceName;

    // 扩展 JSON 字符串
    private String detail;
}
