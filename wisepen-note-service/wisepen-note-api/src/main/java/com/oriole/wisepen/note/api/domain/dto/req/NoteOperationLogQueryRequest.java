package com.oriole.wisepen.note.api.domain.dto.req;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.oriole.wisepen.common.core.jackson.TimestampLocalDateTimeDeserializer;
import com.oriole.wisepen.common.core.jackson.TimestampLocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NoteOperationLogQueryRequest implements Serializable {
    private String resourceId;
    private String userId;
    @JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
    @JsonDeserialize(using = TimestampLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
    @JsonDeserialize(using = TimestampLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    private int page = 1;
    private int size = 20;
}
