package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.resource.domain.dto.req.ResourceOperationLogQueryRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceOperationLogResponse;

public interface IResourceOperationLogService {

    PageR<ResourceOperationLogResponse> listResourceOperationLogs(ResourceOperationLogQueryRequest request);
}
