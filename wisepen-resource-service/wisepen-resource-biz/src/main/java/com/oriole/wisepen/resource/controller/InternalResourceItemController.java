package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.core.domain.enums.list.SortDirectionEnum;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.resource.domain.dto.*;
import com.oriole.wisepen.resource.domain.dto.req.AppendResourceOperationLogRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceOperationLogResponse;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import com.oriole.wisepen.resource.service.IGroupResService;
import com.oriole.wisepen.resource.service.IResourceService;
import com.oriole.wisepen.resource.service.ITagService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/resource")
@RequiredArgsConstructor
public class InternalResourceItemController implements RemoteResourceService {

    // 内部 Feign 接口，不打 @Log。被调用方（Document/User Controller）负责在自己的入口处审计。
    private final IResourceService resourceService;
    private final IGroupResService groupResService;
    private final ITagService tagService;

    // 注册/新增资源摘要
    @PostMapping("/addRes")
    public R<String> createResource(@Validated @RequestBody ResourceCreateReqDTO dto) {
        String resourceId = resourceService.createResourceItem(dto);
        return R.ok(resourceId);
    }

    // 同步修改资源属性
    @PostMapping("/changeResAttr")
    public R<Void> updateAttributes(@Validated @RequestBody ResourceUpdateReqDTO dto) {
        resourceService.updateResourceAttributes(dto);
        return R.ok();
    }

    @PostMapping("/getResourceInfo")
    public R<ResourceItemResponse> getResourceInfo(@Validated @RequestBody ResourceInfoGetReqDTO dto) {
        ResourceItemResponse response = resourceService.getResourceInfo(dto);
        return R.ok(response);
    }

    // 内部鉴权接口，供下游微服务在执行敏感操作（如：导出PDF、分享链接）前进行硬核鉴权
    @PostMapping("/checkResPermission")
    public R<ResourceCheckPermissionResDTO> checkResPermission(ResourceCheckPermissionReqDTO dto) {
        ResourceCheckPermissionResDTO hasPermission = resourceService.checkPermission(dto);
        return R.ok(hasPermission);
    }

    // 小组解散：软删除 Tag 树与配置
    @PostMapping("/dissolveGroup")
    public R<Void> dissolveGroup(@RequestParam("groupId") Long groupId) {
        tagService.softRemoveAllTagByGroupId(groupId.toString());
        groupResService.softRemoveGroupResConfigByGroupId(groupId.toString());
        return R.ok();
    }

    @PostMapping("/appendOperationLog")
    public R<Void> appendOperationLog(@Validated @RequestBody AppendResourceOperationLogRequest dto) {
        resourceService.appendResourceOperationLog(dto);
        return R.ok();
    }

    /**
     * 兼容 Feign：全库分页，等价于 {@code GET /resource/item/operationLogs} 不传 userId、resourceId。
     */
    @CheckRole(IdentityType.ADMIN)
    @GetMapping("/pageAllOperationLogs")
    public R<PageR<ResourceOperationLogResponse>> pageAllOperationLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortDir", required = false) SortDirectionEnum sortDir) {
        return R.ok(resourceService.listResourceOperationLogs(null, null, page, size, sortDir, false, null));
    }

}
