package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.domain.enums.list.QueryLogicEnum;
import com.oriole.wisepen.common.core.domain.enums.list.SortDirectionEnum;
import com.oriole.wisepen.resource.domain.dto.*;
import com.oriole.wisepen.resource.domain.dto.req.AppendResourceOperationLogRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceRenameRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateActionPermissionRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateTagsRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceOperationLogResponse;
import com.oriole.wisepen.resource.enums.ResourceSortBy;

import java.util.List;

public interface IResourceService {

    // 断言资源归某用户所有
    void assertResourceOwner(String resourceId, String userId);

    // ToUser：重命名、变更Tag、列出资源
    void renameResource(ResourceRenameRequest req);

    void updateResourceTags(ResourceUpdateTagsRequest req);

    void recordTagUpdateOperationLog(String resourceId, String groupId);

    void updateResourceActionPermission(ResourceUpdateActionPermissionRequest req);

    PageR<ResourceItemResponse> listResources(String currentUserId,
                                              String groupId, GroupRoleType userGroupRole,
                                              List<String> tagIds, QueryLogicEnum tagQueryLogicMode,
                                              String resourceType, int page, int size,
                                              ResourceSortBy sortBy, SortDirectionEnum sortDir);

    void softRemoveResources(List<String> resourceIds);

    // 内部：标签节点变更后、标签节点删除后（权限重新计算/移除标签）；权限重新计算
    void afterTagNodeChanged(List<String> changedTagIds, Boolean isPersonalTag);

    void afterTagNodeDeleted(List<String> deletedTagIds, Boolean isPersonalTag, Boolean isPathTag);

    void calculateResourceGroupAcl(String resourceId);

    // ToService：增加、移除、更新资源；检查特定资源的权限

    String createResourceItem(ResourceCreateReqDTO dto);

    void hardRemoveResources(List<String> resourceIds);

    void updateResourceAttributes(ResourceUpdateReqDTO dto);

    ResourceItemResponse getResourceInfo(ResourceInfoGetReqDTO dto);

    ResourceCheckPermissionResDTO checkPermission(ResourceCheckPermissionReqDTO dto);

    void stripGroupPermission(List<String> trashedTagIds);

    void appendResourceOperationLog(AppendResourceOperationLogRequest req);

    /**
     * @param enforceResourceViewCheck 为 true 时，涉及 resourceId 会走 {@link #getResourceInfo} 做 VIEW 校验；
     *                                   入口已用 {@code @CheckRole(ADMIN)} 鉴权的管理员接口应传 {@code false}，避免业务层依赖安全上下文。
     */
    PageR<ResourceOperationLogResponse> listResourceOperationLogsForCurrentUser(Long subjectUserId,
                                                                                String resourceId, int page, int size,
                                                                                SortDirectionEnum sortDir,
                                                                                boolean enforceResourceViewCheck);

    /**
     * @param enforceResourceViewCheck 为 true 时先 {@link #getResourceInfo}；管理员入口已鉴权时传 {@code false}。
     */
    PageR<ResourceOperationLogResponse> listResourceOperationLogsForResource(Long currentUserId,
                                                                             String resourceId, int page, int size,
                                                                             SortDirectionEnum sortDir,
                                                                             boolean enforceResourceViewCheck);

    //全部资源操作历史
    PageR<ResourceOperationLogResponse> listAllResourceOperationLogs(int page, int size, SortDirectionEnum sortDir);
}