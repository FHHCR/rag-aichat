package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.dto.CreateKnowledgeBaseRequest;
import com.rag.aichat.dto.KnowledgeBaseResponse;
import com.rag.aichat.dto.UpdateKnowledgeBaseRequest;
import com.rag.aichat.entity.KnowledgeBase;
import com.rag.aichat.mapper.KnowledgeBaseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final DocumentFileService documentFileService;

    public KnowledgeBaseService(KnowledgeBaseMapper knowledgeBaseMapper, DocumentFileService documentFileService) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentFileService = documentFileService;
    }

    @Transactional
    public KnowledgeBaseResponse create(CreateKnowledgeBaseRequest request, String createdById) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(request.getName().trim());
        knowledgeBase.setDescription(normalize(request.getDescription()));
        knowledgeBase.setStatus("ACTIVE");
        knowledgeBase.setCreatedById(createdById);
        knowledgeBase.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        knowledgeBase.setCreatedAt(now);
        knowledgeBase.setUpdatedAt(now);
        knowledgeBaseMapper.insert(knowledgeBase);
        return toResponse(knowledgeBase);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseResponse> list() {
        return knowledgeBaseMapper.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeBaseResponse get(String id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public KnowledgeBaseResponse update(String id, UpdateKnowledgeBaseRequest request) {
        KnowledgeBase knowledgeBase = getEntity(id);
        knowledgeBase.setName(request.getName().trim());
        knowledgeBase.setDescription(normalize(request.getDescription()));
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.update(knowledgeBase);
        return toResponse(knowledgeBase);
    }

    @Transactional
    public void delete(String id) {
        if (knowledgeBaseMapper.countById(id) == 0) {
            throw notFound();
        }
        documentFileService.deleteByKnowledgeBase(id);
        knowledgeBaseMapper.deleteById(id);
    }

    private KnowledgeBase getEntity(String id) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.findById(id);
        if (knowledgeBase == null) {
            throw notFound();
        }
        return knowledgeBase;
    }

    private BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.value(), "knowledge base not found");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private KnowledgeBaseResponse toResponse(KnowledgeBase knowledgeBase) {
        KnowledgeBaseResponse response = new KnowledgeBaseResponse();
        response.setId(knowledgeBase.getId());
        response.setName(knowledgeBase.getName());
        response.setDescription(knowledgeBase.getDescription());
        response.setStatus(knowledgeBase.getStatus());
        response.setCreatedAt(knowledgeBase.getCreatedAt());
        response.setUpdatedAt(knowledgeBase.getUpdatedAt());
        return response;
    }
}
