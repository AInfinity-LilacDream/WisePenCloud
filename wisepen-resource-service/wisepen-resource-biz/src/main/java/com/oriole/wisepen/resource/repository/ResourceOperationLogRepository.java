package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceOperationLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link ResourceOperationLogEntity} 持久化，集合名 {@value com.oriole.wisepen.resource.domain.entity.ResourceOperationLogEntity#COLLECTION_NAME}。
 */
@Repository
public interface ResourceOperationLogRepository extends MongoRepository<ResourceOperationLogEntity, String> {

    Page<ResourceOperationLogEntity> findByUserId(Long userId, Pageable pageable);

    Page<ResourceOperationLogEntity> findByUserIdAndResourceId(Long userId, String resourceId, Pageable pageable);

    Page<ResourceOperationLogEntity> findByResourceId(String resourceId, Pageable pageable);
}
