package com.oriole.wisepen.resource.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.req.NoteOperationLogQueryRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteOperationLogResponse;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.resource.constant.ResourceConstants;
import com.oriole.wisepen.resource.domain.dto.req.ResourceOperationLogQueryRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceOperationLogResponse;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IResourceOperationLogService;
import com.oriole.wisepen.system.api.domain.dto.SysOperLogDTO;
import com.oriole.wisepen.system.api.domain.dto.SysOperLogQueryDTO;
import com.oriole.wisepen.system.api.feign.RemoteLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourceOperationLogServiceImpl implements IResourceOperationLogService {

    private static final String SOURCE_SYS_OPER_LOG = "SYS_OPER_LOG";
    private static final String SOURCE_NOTE_OPERATION_LOG = "NOTE_OPERATION_LOG";

    private static final String OP_CREATE = "CREATE";
    private static final String OP_DELETE = "DELETE";
    private static final String OP_RENAME = "RENAME";
    private static final String OP_TAG = "TAG";
    private static final String OP_PERMISSION = "PERMISSION";
    private static final String OP_UPDATE_CONTENT = "UPDATE_CONTENT";

    private static final String URL_RESOURCE_CREATE = "/internal/resource/addRes";
    private static final String URL_RESOURCE_DELETE = "/resource/item/removeResources";
    private static final String URL_RESOURCE_RENAME = "/resource/item/renameResource";
    private static final String URL_RESOURCE_TAG = "/resource/item/changeResourceTags";
    private static final String URL_RESOURCE_PERMISSION = "/resource/item/changeResourceActionPermission";

    private static final List<String> RESOURCE_OPERATION_URLS = List.of(
            URL_RESOURCE_CREATE,
            URL_RESOURCE_DELETE,
            URL_RESOURCE_RENAME,
            URL_RESOURCE_TAG,
            URL_RESOURCE_PERMISSION
    );

    private final RemoteLogService remoteLogService;
    private final RemoteNoteService remoteNoteService;
    private final ResourceItemRepository resourceItemRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public PageR<ResourceOperationLogResponse> listResourceOperationLogs(ResourceOperationLogQueryRequest request) {
        ResourceOperationLogQueryRequest safeRequest = request == null ? new ResourceOperationLogQueryRequest() : request;
        int page = safeRequest.getPage() > 0 ? safeRequest.getPage() : 1;
        int size = safeRequest.getSize() > 0 ? safeRequest.getSize() : 20;
        int candidateSize = page * size;

        LogPage sysLogs = listSystemLogs(safeRequest, candidateSize);
        PageR<NoteOperationLogResponse> noteLogs = listNoteLogs(safeRequest, candidateSize);

        List<ResourceOperationLogResponse> merged = new ArrayList<>();
        merged.addAll(sysLogs.list());
        merged.addAll(toResourceLogs(noteLogs.getList()));
        merged.sort(Comparator.comparing(
                ResourceOperationLogResponse::getOperationTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        int fromIndex = Math.min((page - 1) * size, merged.size());
        int toIndex = Math.min(fromIndex + size, merged.size());
        PageR<ResourceOperationLogResponse> result = new PageR<>(sysLogs.total() + noteLogs.getTotal(), page, size);
        result.addAll(merged.subList(fromIndex, toIndex));
        return result;
    }

    private LogPage listSystemLogs(ResourceOperationLogQueryRequest request, int candidateSize) {
        if (StringUtils.hasText(request.getResourceId())) {
            return listAndFilterAllSystemLogs(request);
        }

        PageR<SysOperLogDTO> page = fetchSystemLogs(request, 1, candidateSize);
        List<ResourceOperationLogResponse> list = page.getList().stream()
                .map(log -> toResourceLog(log, null))
                .flatMap(Optional::stream)
                .toList();
        return new LogPage(list, page.getTotal());
    }

    private LogPage listAndFilterAllSystemLogs(ResourceOperationLogQueryRequest request) {
        int fetchPage = 1;
        int fetchSize = 200;
        List<ResourceOperationLogResponse> filtered = new ArrayList<>();

        while (true) {
            PageR<SysOperLogDTO> page = fetchSystemLogs(request, fetchPage, fetchSize);
            page.getList().stream()
                    .map(log -> toResourceLog(log, request.getResourceId()))
                    .flatMap(Optional::stream)
                    .forEach(filtered::add);

            if ((long) fetchPage * fetchSize >= page.getTotal()) {
                break;
            }
            fetchPage++;
        }
        return new LogPage(filtered, filtered.size());
    }

    private PageR<SysOperLogDTO> fetchSystemLogs(ResourceOperationLogQueryRequest request, int page, int size) {
        SysOperLogQueryDTO query = new SysOperLogQueryDTO();
        query.setOperUrls(RESOURCE_OPERATION_URLS);
        query.setOperUserId(request.getUserId());
        query.setStartTime(request.getStartTime());
        query.setEndTime(request.getEndTime());
        query.setStatus(0);
        query.setPage(page);
        query.setSize(size);

        R<PageR<SysOperLogDTO>> response = remoteLogService.listLogs(query);
        return response != null && response.getData() != null ? response.getData() : new PageR<>(0, page, size);
    }

    private PageR<NoteOperationLogResponse> listNoteLogs(ResourceOperationLogQueryRequest request, int candidateSize) {
        NoteOperationLogQueryRequest query = new NoteOperationLogQueryRequest();
        query.setResourceId(request.getResourceId());
        query.setUserId(request.getUserId() == null ? null : request.getUserId().toString());
        query.setStartTime(request.getStartTime());
        query.setEndTime(request.getEndTime());
        query.setPage(1);
        query.setSize(candidateSize);

        R<PageR<NoteOperationLogResponse>> response = remoteNoteService.listNoteOperationLogs(query);
        return response != null && response.getData() != null ? response.getData() : new PageR<>(0, 1, candidateSize);
    }

    private Optional<ResourceOperationLogResponse> toResourceLog(SysOperLogDTO log, String requiredResourceId) {
        String operationType = resolveOperationType(log.getOperUrl());
        if (operationType == null) {
            return Optional.empty();
        }

        Set<String> resourceIds = extractResourceIds(log);
        if (StringUtils.hasText(requiredResourceId) && !resourceIds.contains(requiredResourceId)) {
            return Optional.empty();
        }

        String resourceId = StringUtils.hasText(requiredResourceId)
                ? requiredResourceId
                : resourceIds.stream().findFirst().orElse(null);

        ResourceOperationLogResponse response = new ResourceOperationLogResponse();
        response.setLogId(log.getId() == null ? null : log.getId().toString());
        response.setSource(SOURCE_SYS_OPER_LOG);
        response.setOperationType(operationType);
        response.setOperationTime(log.getOperTime());
        response.setResourceId(resourceId);
        response.setResourceName(resolveResourceName(resourceId, log));
        response.setUserId(log.getOperUserId());
        response.setContentSummary(log.getTitle());
        return Optional.of(response);
    }

    private List<ResourceOperationLogResponse> toResourceLogs(List<NoteOperationLogResponse> noteLogs) {
        if (noteLogs == null || noteLogs.isEmpty()) {
            return List.of();
        }
        return noteLogs.stream().map(log -> {
            ResourceOperationLogResponse response = new ResourceOperationLogResponse();
            response.setLogId(log.getId());
            response.setSource(SOURCE_NOTE_OPERATION_LOG);
            response.setOperationType(OP_UPDATE_CONTENT);
            response.setOperationTime(log.getTimestamp());
            response.setResourceId(log.getResourceId());
            response.setResourceName(resolveResourceName(log.getResourceId(), null));
            response.setUserId(parseLong(log.getUserId()));
            response.setContentSummary(log.getContentSummary());
            return response;
        }).toList();
    }

    private String resolveOperationType(String operUrl) {
        return switch (operUrl) {
            case URL_RESOURCE_CREATE -> OP_CREATE;
            case URL_RESOURCE_DELETE -> OP_DELETE;
            case URL_RESOURCE_RENAME -> OP_RENAME;
            case URL_RESOURCE_TAG -> OP_TAG;
            case URL_RESOURCE_PERMISSION -> OP_PERMISSION;
            default -> null;
        };
    }

    private Set<String> extractResourceIds(SysOperLogDTO log) {
        Set<String> resourceIds = new LinkedHashSet<>();
        collectNamedValues(readTree(log.getOperParam()), resourceIds);
        collectNamedValues(readTree(log.getJsonResult()), resourceIds);

        if (URL_RESOURCE_DELETE.equals(log.getOperUrl()) && resourceIds.isEmpty()) {
            collectTextValues(readTree(log.getOperParam()), resourceIds);
        }
        if (URL_RESOURCE_CREATE.equals(log.getOperUrl())) {
            JsonNode data = readTree(log.getJsonResult()).path("data");
            if (data.isTextual()) {
                resourceIds.add(data.asText());
            }
        }
        return resourceIds;
    }

    private void collectNamedValues(JsonNode node, Set<String> values) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode value = entry.getValue();
                if (isResourceIdField(fieldName)) {
                    collectTextValues(value, values);
                } else {
                    collectNamedValues(value, values);
                }
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectNamedValues(child, values));
        }
    }

    private boolean isResourceIdField(String fieldName) {
        return "resourceId".equals(fieldName)
                || "resourceIds".equals(fieldName)
                || "skillId".equals(fieldName);
    }

    private void collectTextValues(JsonNode node, Set<String> values) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isTextual() && StringUtils.hasText(node.asText())) {
            values.add(node.asText());
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectTextValues(child, values));
        }
    }

    private String resolveResourceName(String resourceId, SysOperLogDTO log) {
        String nameFromLog = log == null ? null : firstTextValue(log, "resourceName", "filename", "title", "newName");
        if (StringUtils.hasText(nameFromLog)) {
            return nameFromLog;
        }
        if (!StringUtils.hasText(resourceId)) {
            return null;
        }
        return findResource(resourceId)
                .map(ResourceItemEntity::getResourceName)
                .orElse(null);
    }

    private Optional<ResourceItemEntity> findResource(String resourceId) {
        Optional<ResourceItemEntity> active = resourceItemRepository.findById(resourceId);
        if (active.isPresent()) {
            return active;
        }
        Query query = Query.query(Criteria.where("_id").is(resourceId));
        return Optional.ofNullable(mongoTemplate.findOne(
                query,
                ResourceItemEntity.class,
                ResourceConstants.RESOURCE_TRASH_COLLECTION
        ));
    }

    private String firstTextValue(SysOperLogDTO log, String... fieldNames) {
        JsonNode[] roots = {readTree(log.getOperParam()), readTree(log.getJsonResult())};
        for (JsonNode root : roots) {
            for (String fieldName : fieldNames) {
                String value = findTextValue(root, fieldName);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private String findTextValue(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            JsonNode direct = node.get(fieldName);
            if (direct != null && direct.isTextual()) {
                return direct.asText();
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                String value = findTextValue(fields.next().getValue(), fieldName);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                String value = findTextValue(child, fieldName);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private JsonNode readTree(String json) {
        if (!StringUtils.hasText(json)) {
            return objectMapper.missingNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception ignored) {
            return objectMapper.missingNode();
        }
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record LogPage(List<ResourceOperationLogResponse> list, long total) {
    }
}
