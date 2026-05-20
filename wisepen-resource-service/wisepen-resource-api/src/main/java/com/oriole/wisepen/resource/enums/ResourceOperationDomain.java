package com.oriole.wisepen.resource.enums;

/**
 * 资源操作日志业务域：与 {@code operation_type} 组合表达完整语义，
 * 例如 domain=NOTE + operation_type=CREATE 表示「笔记创建」。
 */
public enum ResourceOperationDomain {
    DOCUMENT,
    NOTE,
}
