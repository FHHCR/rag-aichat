package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.SystemStatusResponse;
import com.rag.aichat.service.SystemStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/status")
    public ApiResponse<SystemStatusResponse> status() {
        return ApiResponse.ok(systemStatusService.status());
    }
}

