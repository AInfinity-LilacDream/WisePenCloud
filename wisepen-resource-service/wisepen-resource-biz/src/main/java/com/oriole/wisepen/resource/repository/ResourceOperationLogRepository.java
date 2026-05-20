package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceOperationLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link ResourceOperationLogEntity} 持久化，集合名 {@value com.oriole.wisepen.resource.domain.entity.ResourceOperationLogEntity#COLLECTION_NAME}。
 */
@Repository
public interface ResourceOperationLogRepository extends MongoRepository<ResourceOperationLogEntity, String> {
}
