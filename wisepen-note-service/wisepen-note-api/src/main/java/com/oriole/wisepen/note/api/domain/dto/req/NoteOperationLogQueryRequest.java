package com.oriole.wisepen.note.api.domain.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NoteOperationLogQueryRequest implements Serializable {
    private String resourceId;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int page;
    private int size;
}
