package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.dto.CreateKnowledgeBaseRequest;
import com.rag.aichat.dto.KnowledgeBaseResponse;
import com.rag.aichat.dto.UpdateKnowledgeBaseRequest;
import com.rag.aichat.entity.KnowledgeBase;
import com.rag.aichat.repository.KnowledgeBaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    @Transactional
    public KnowledgeBaseResponse create(CreateKnowledgeBaseRequest request, String createdById) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(request.getName().trim());
        knowledgeBase.setDescription(normalize(request.getDescription()));
        knowledgeBase.setStatus("ACTIVE");
        knowledgeBase.setCreatedById(createdById);
        return toResponse(knowledgeBaseRepository.save(knowledgeBase));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseResponse> list() {
        return knowledgeBaseRepository.findAllByOrderByCreatedAtDesc().stream()
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
        return toResponse(knowledgeBaseRepository.save(knowledgeBase));
    }

    @Transactional
    public void delete(String id) {
        if (!knowledgeBaseRepository.existsById(id)) {
            throw notFound();
        }
        knowledgeBaseRepository.deleteById(id);
    }

    private KnowledgeBase getEntity(String id) {
        return knowledgeBaseRepository.findById(id).orElseThrow(this::notFound);
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
