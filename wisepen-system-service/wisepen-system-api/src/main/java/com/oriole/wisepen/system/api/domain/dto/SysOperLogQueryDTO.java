package com.oriole.wisepen.system.api.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.oriole.wisepen.common.core.jackson.TimestampLocalDateTimeDeserializer;
import com.oriole.wisepen.common.core.jackson.TimestampLocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysOperLogQueryDTO implements Serializable {
    private List<String> operUrls;
    private Long operUserId;
    @JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
    @JsonDeserialize(using = TimestampLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
    @JsonDeserialize(using = TimestampLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    private Integer status;
    private int page = 1;
    private int size = 20;
}
