package com.oriole.wisepen.resource.enums;

/**
 * 资源操作流水中的域内操作码（存库字段 {@code operation_type}），与 {@link ResourceOperationDomain} 组合表达语义。
 * <p>为单一来源：HTTP 入参校验见 {@link com.oriole.wisepen.resource.validation.ResourceOperationTypeCode}；
 * 业务代码请使用本枚举常量，避免手写字符串漂移。</p>
 */
public enum ResourceOperationType {

    CREATE,
    RENAME,
    TAG_UPDATE,
    PERMISSION_UPDATE,
    META_UPDATE,
    DELETE,
    PURGE,
}
