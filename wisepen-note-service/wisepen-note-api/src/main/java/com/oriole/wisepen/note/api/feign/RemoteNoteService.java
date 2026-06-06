package com.oriole.wisepen.note.api.feign;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.req.NoteOperationLogQueryRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteOperationLogResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteNoteService", value = "wisepen-note-service")
public interface RemoteNoteService {

    @GetMapping("/internal/note/getNoteLatestVersion")
    R<NoteSnapshotResponse> getNoteLatestVersion(@RequestParam("resourceId") String resourceId);

    @Operation(summary = "查询笔记操作日志")
    @PostMapping("/internal/note/listNoteOperationLogs")
    R<PageR<NoteOperationLogResponse>> listNoteOperationLogs(@RequestBody NoteOperationLogQueryRequest request);
}
