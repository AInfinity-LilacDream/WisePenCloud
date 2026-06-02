package com.oriole.wisepen.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.note.api.domain.dto.req.NoteOperationLogQueryRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteOperationLogResponse;
import com.oriole.wisepen.note.api.domain.mq.NoteOperationLogMessage;
import com.oriole.wisepen.note.domain.entity.NoteOperationLogEntity;
import com.oriole.wisepen.note.repository.NoteOperationLogRepository;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteOperationLogServiceImpl implements INoteOperationLogService {

    private final NoteOperationLogRepository noteOperationLogRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void batchSave(NoteOperationLogMessage msg) {
        List<NoteOperationLogEntity> entities = msg.getEntries().stream().map(entry -> {
            NoteOperationLogEntity entity = NoteOperationLogEntity.builder()
                    .resourceId(msg.getResourceId())
                    .build();
            BeanUtil.copyProperties(entry, entity, "timestamp");
            entity.setTimestamp(entry.getTimestamp() == null
                    ? LocalDateTime.now()
                    : LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getTimestamp()), ZoneId.systemDefault()));
            return entity;
        }).toList();
        noteOperationLogRepository.saveAll(entities);
    }

    @Override
    public PageR<NoteOperationLogResponse> listOperationLogs(String resourceId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<NoteOperationLogEntity> entityPage = noteOperationLogRepository.findByResourceIdOrderByTimestampDesc(resourceId, pageable);
        PageR<NoteOperationLogResponse> pageR = new PageR<>(entityPage.getTotalElements(), page, size);

        List<NoteOperationLogResponse> responses = entityPage.getContent().stream().map(entity -> {
            NoteOperationLogResponse response = new NoteOperationLogResponse();
            BeanUtil.copyProperties(entity, response);
            return response;
        }).toList();

        pageR.addAll(responses);
        return pageR;
    }

    @Override
    public PageR<NoteOperationLogResponse> listOperationLogs(NoteOperationLogQueryRequest request) {
        NoteOperationLogQueryRequest safeRequest = request == null ? new NoteOperationLogQueryRequest() : request;
        int page = safeRequest.getPage() > 0 ? safeRequest.getPage() : 1;
        int size = safeRequest.getSize() > 0 ? safeRequest.getSize() : 20;

        Query query = buildQuery(safeRequest)
                .with(Sort.by(Sort.Direction.DESC, "timestamp"));
        long total = mongoTemplate.count(query, NoteOperationLogEntity.class);

        query.skip((long) (page - 1) * size).limit(size);
        List<NoteOperationLogResponse> responses = mongoTemplate.find(query, NoteOperationLogEntity.class).stream()
                .map(entity -> BeanUtil.copyProperties(entity, NoteOperationLogResponse.class))
                .toList();

        PageR<NoteOperationLogResponse> pageR = new PageR<>(total, page, size);
        pageR.addAll(responses);
        return pageR;
    }

    @Override
    public void deleteAllOpLogsByResourceIds(List<String> resourceIds) {
        noteOperationLogRepository.deleteByResourceIdIn(resourceIds);
    }

    private Query buildQuery(NoteOperationLogQueryRequest request) {
        Criteria criteria = new Criteria();
        if (request.getResourceId() != null && !request.getResourceId().isBlank()) {
            criteria.and("resourceId").is(request.getResourceId());
        }
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            criteria.and("userId").is(request.getUserId());
        }
        if (request.getStartTime() != null || request.getEndTime() != null) {
            Criteria timestamp = criteria.and("timestamp");
            if (request.getStartTime() != null) {
                timestamp.gte(request.getStartTime());
            }
            if (request.getEndTime() != null) {
                timestamp.lte(request.getEndTime());
            }
        }
        return Query.query(criteria);
    }
}
