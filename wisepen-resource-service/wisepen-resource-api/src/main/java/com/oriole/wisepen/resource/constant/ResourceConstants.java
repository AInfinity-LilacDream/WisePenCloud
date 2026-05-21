package com.oriole.wisepen.resource.constant;

public interface ResourceConstants {
    String PERSONAL_GROUP_PREFIX = "p_";

    // 系统级保留路径节点名称
    String ROOT_TAG_NAME = "/";
    String TRASH_TAG_NAME = ".Trash";

    String RESOURCE_TRASH_COLLECTION = "wisepen_resource_trash";
    String CONFIG_TRASH_COLLECTION = "wisepen_group_res_config_trash";
    String TAGS_TRASH_COLLECTION = "wisepen_tags_trash";

    /**
     * 资源操作流水查询单页条数上限（通过翻页仍可拉取全量）。
     */
    int RESOURCE_OPERATION_LOG_MAX_PAGE_SIZE = 2000;
}
