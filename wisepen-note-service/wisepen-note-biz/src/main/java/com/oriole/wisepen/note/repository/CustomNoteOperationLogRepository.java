package com.oriole.wisepen.note.repository;

import com.oriole.wisepen.note.api.domain.dto.req.NoteOperationLogQueryRequest;
import com.oriole.wisepen.note.domain.entity.NoteOperationLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomNoteOperationLogRepository {

    private final MongoTemplate mongoTemplate;

    public CustomNoteOperationLogRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 按可选条件查询笔记操作日志，并返回 Spring Data 分页结果。
     */
    public Page<NoteOperationLogEntity> findOperationLogs(NoteOperationLogQueryRequest request, Pageable pageable) {
        List<Criteria> allCriteria = new ArrayList<>();
        if (StringUtils.hasText(request.getResourceId())) {
            allCriteria.add(Criteria.where("resourceId").is(request.getResourceId()));
        }
        if (StringUtils.hasText(request.getUserId())) {
            allCriteria.add(Criteria.where("userId").is(request.getUserId()));
        }
        if (request.getStartTime() != null || request.getEndTime() != null) {
            Criteria timestamp = Criteria.where("timestamp");
            if (request.getStartTime() != null) {
                timestamp.gte(request.getStartTime());
            }
            if (request.getEndTime() != null) {
                timestamp.lte(request.getEndTime());
            }
            allCriteria.add(timestamp);
        }

        Query query = allCriteria.isEmpty()
                ? new Query()
                : new Query(new Criteria().andOperator(allCriteria.toArray(new Criteria[0])));
        long total = mongoTemplate.count(query, NoteOperationLogEntity.class);

        query.with(pageable);
        List<NoteOperationLogEntity> list = mongoTemplate.find(query, NoteOperationLogEntity.class);

        return new PageImpl<>(list, pageable, total);
    }
}
