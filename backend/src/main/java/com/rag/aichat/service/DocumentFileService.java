package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.config.RagProperties;
import com.rag.aichat.mapper.DocumentChunkMapper;
import com.rag.aichat.mapper.DocumentFileMapper;
import com.rag.aichat.mapper.KnowledgeBaseMapper;
import com.rag.aichat.mapper.VectorChunkMapper;
import com.rag.aichat.dto.DocumentFileResponse;
import com.rag.aichat.dto.DocumentChunkView;
import com.rag.aichat.entity.DocumentFile;
import com.rag.aichat.entity.KnowledgeBase;
import org.apache.tika.Tika;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentFileService {

    private static final String PARSER = "TIKA";
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private final DocumentFileMapper documentFileMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final VectorChunkMapper vectorChunkMapper;
    private final OpenAiCompatibleClient openAiCompatibleClient;
    private final RagProperties ragProperties;
    private final VectorSearchService vectorSearchService;

    public DocumentFileService(DocumentFileMapper documentFileMapper,
                               DocumentChunkMapper documentChunkMapper,
                               KnowledgeBaseMapper knowledgeBaseMapper,
                               VectorChunkMapper vectorChunkMapper,
                               OpenAiCompatibleClient openAiCompatibleClient,
                               RagProperties ragProperties,
                               VectorSearchService vectorSearchService) {
        this.documentFileMapper = documentFileMapper;
        this.documentChunkMapper = documentChunkMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.vectorChunkMapper = vectorChunkMapper;
        this.openAiCompatibleClient = openAiCompatibleClient;
        this.ragProperties = ragProperties;
        this.vectorSearchService = vectorSearchService;
    }

    @Transactional
    public DocumentFileResponse upload(String knowledgeBaseId, MultipartFile file, String uploadedById) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.findById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), 404, "knowledge base not found");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "file size must be at most 50MB");
        }

        String fileId = UUID.randomUUID().toString();
        Path directory = Paths.get(ragProperties.getUpload().getDir());
        try {
            Files.createDirectories(directory);
            Path path = directory.resolve(fileId + "_" + safeName(file.getOriginalFilename())).normalize();
            if (!path.startsWith(directory.normalize())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "invalid file name");
            }
            Files.copy(file.getInputStream(), path);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "file save failed: " + e.getMessage());
        }

        DocumentFile entity = new DocumentFile();
        entity.setId(fileId);
        entity.setKnowledgeBaseId(knowledgeBase.getId());
        entity.setFileName(file.getOriginalFilename());
        entity.setOriginalPath(Paths.get(ragProperties.getUpload().getDir(), fileId + "_" + safeName(file.getOriginalFilename())).toString());
        entity.setMimeType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setParser(PARSER);
        entity.setStatus(DocumentFile.STATUS_UPLOADED);
        entity.setChunkCount(0);
        entity.setUploadedById(uploadedById);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        documentFileMapper.insert(entity);
        DocumentFile saved = entity;

        return indexFile(saved, uploadedById);
    }

    @Transactional(readOnly = true)
    public List<DocumentFileResponse> list(String knowledgeBaseId) {
        return documentFileMapper.findByKnowledgeBaseIdOrderByCreatedAtDesc(knowledgeBaseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentFileResponse get(String knowledgeBaseId, String fileId) {
        return toResponse(findOwnedFile(knowledgeBaseId, fileId));
    }

    @Transactional
    public DocumentFileResponse reindex(String knowledgeBaseId, String fileId, String actorId) {
        return indexFile(findOwnedFile(knowledgeBaseId, fileId), actorId);
    }

    @Transactional
    public void delete(String knowledgeBaseId, String fileId) {
        DocumentFile file = findOwnedFile(knowledgeBaseId, fileId);
        documentChunkMapper.deleteByDocumentFileId(file.getId());
        documentFileMapper.deleteById(file.getId());
        try {
            Files.deleteIfExists(Paths.get(file.getOriginalPath()));
        } catch (IOException ignored) {
        }
    }

    @Transactional
    public void deleteByKnowledgeBase(String knowledgeBaseId) {
        for (DocumentFile file : documentFileMapper.findByKnowledgeBaseIdOrderByCreatedAtDesc(knowledgeBaseId)) {
            documentChunkMapper.deleteByDocumentFileId(file.getId());
            try {
                Files.deleteIfExists(Paths.get(file.getOriginalPath()));
            } catch (IOException ignored) {
            }
        }
        documentFileMapper.deleteByKnowledgeBaseId(knowledgeBaseId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.NOT_SUPPORTED)
    private DocumentFileResponse indexFile(DocumentFile file, String actorId) {
        try {
            file.setStatus(DocumentFile.STATUS_PARSING);
            documentFileMapper.update(file);
            String text = parseText(file);
            if (text.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "file text is empty");
            }
            file.setStatus(DocumentFile.STATUS_PARSED);
            documentFileMapper.update(file);

            List<String> chunks = chunkText(text, ragProperties.getRag().getChunkSize(), ragProperties.getRag().getChunkOverlap());
            if (chunks.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "no chunks generated");
            }

            file.setStatus(DocumentFile.STATUS_INDEXING);
            documentFileMapper.update(file);
            List<float[]> vectors = openAiCompatibleClient.embed(chunks);
            vectorChunkMapper.deleteByDocumentFileId(file.getId());
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = UUID.randomUUID().toString();
                vectorSearchService.insert(chunkId, file.getId(), file.getKnowledgeBaseId(), i, chunks.get(i), "openai-compatible", ragProperties.getEmbedding().getModel(), vectors.get(i));
            }

            file.setStatus(DocumentFile.STATUS_INDEXED);
            file.setChunkCount(chunks.size());
            file.setParseError(null);
            file.setUpdatedAt(LocalDateTime.now());
            documentFileMapper.update(file);
            return toResponse(file);
        } catch (Exception e) {
            if (e instanceof BusinessException businessException) {
                file.setStatus(isIndexingError(businessException) ? DocumentFile.STATUS_INDEX_FAILED : DocumentFile.STATUS_PARSED_FAILED);
                file.setParseError(truncate(businessException.getMessage(), 1900));
            } else if (e instanceof DataAccessException) {
                file.setStatus(DocumentFile.STATUS_INDEX_FAILED);
                file.setParseError(truncate("pgvector or database write failed: " + e.getMessage(), 1900));
            } else {
                file.setStatus(DocumentFile.STATUS_INDEX_FAILED);
                file.setParseError(truncate("indexing failed: " + e.getMessage(), 1900));
            }
            file.setUpdatedAt(LocalDateTime.now());
            documentFileMapper.update(file);
            return toResponse(file);
        }
    }

    private String parseText(DocumentFile file) {
        try (InputStream input = Files.newInputStream(Paths.get(file.getOriginalPath()))) {
            return new Tika().parseToString(input);
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "tika parse failed: " + e.getMessage());
        }
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        int size = Math.max(100, chunkSize);
        int overlapLength = Math.max(0, Math.min(overlap, size - 1));
        String compact = text.replaceAll("\\r\\n", "\n");
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < compact.length()) {
            int end = Math.min(compact.length(), start + size);
            String chunk = compact.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            if (end == compact.length()) break;
            start = Math.max(start + 1, end - overlapLength);
        }
        return chunks;
    }

    private DocumentFile findOwnedFile(String knowledgeBaseId, String fileId) {
        DocumentFile file = documentFileMapper.findById(fileId);
        if (file == null || !knowledgeBaseId.equals(file.getKnowledgeBaseId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), 404, "document file not found");
        }
        return file;
    }

    private DocumentFileResponse toResponse(DocumentFile file) {
        DocumentFileResponse response = new DocumentFileResponse();
        response.setId(file.getId());
        response.setKnowledgeBaseId(file.getKnowledgeBaseId());
        response.setFileName(file.getFileName());
        response.setOriginalPath(file.getOriginalPath());
        response.setMimeType(file.getMimeType());
        response.setFileSize(file.getFileSize());
        response.setParser(file.getParser());
        response.setStatus(file.getStatus());
        response.setParseError(file.getParseError());
        response.setChunkCount(file.getChunkCount());
        response.setUploadedById(file.getUploadedById());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        return response;
    }

    private boolean isIndexingError(BusinessException e) {
        return e.getMessage() != null && e.getMessage().contains("embedding");
    }

    private String safeName(String originalName) {
        String name = originalName == null ? "upload" : originalName;
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String truncate(String value, int length) {
        if (value == null) return null;
        return value.length() <= length ? value : value.substring(0, length);
    }
}

