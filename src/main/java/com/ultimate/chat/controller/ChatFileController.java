package com.ultimate.chat.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ultimate.chat.dto.ChatAttachmentReq;
import com.ultimate.chat.service.ChatFileService;
import com.ultimate.chat.repository.ChatAttachmentRepository;
import com.ultimate.chat.entity.ChatAttachment;
import com.ultimate.chat.util.EncryptionService;

@RestController
@RequestMapping("/api/chat/files")
public class ChatFileController {

    private final ChatFileService fileService;
    private final ChatAttachmentRepository attachmentRepository;
    private final EncryptionService encryptionService;

    @Value("${ixchat.file.storage.location}")
    private String storageLocation;

    public ChatFileController(
            ChatFileService fileService,
            ChatAttachmentRepository attachmentRepository,
            EncryptionService encryptionService) {

        this.fileService = fileService;
        this.attachmentRepository = attachmentRepository;
        this.encryptionService = encryptionService;
    }

    @PostMapping
    public ChatAttachmentReq uploadFile(
            @RequestParam("file")
            MultipartFile file)
            throws IOException {

        return fileService.saveFile(file);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileId)
            throws IOException {

        // Find attachment by fileId (stored in storagePath) using database query
        Optional<ChatAttachment> attachmentOptional = attachmentRepository.findByStoragePath(fileId);

        if (!attachmentOptional.isPresent()) {
            throw new FileNotFoundException("File metadata not found in database");
        }

        ChatAttachment attachment = attachmentOptional.get();

        if (attachment.getIv() == null || attachment.getIv().isBlank()) {
            throw new IOException("Missing IV for decryption");
        }

        // Resolve the encrypted file on disk
        Path storageDir = Paths.get(storageLocation).toAbsolutePath().normalize();
        Path encryptedFile = storageDir.resolve(attachment.getStoragePath());

        if (!Files.exists(encryptedFile)) {
            throw new FileNotFoundException("Encrypted file not found on disk: " + encryptedFile.toString());
        }

        // Decrypt file to bytes using stored IV
        byte[] decryptedData = encryptionService.decryptToBytes(encryptedFile, attachment.getIv());

        ByteArrayResource resource = new ByteArrayResource(decryptedData);

        String contentType = attachment.getFileType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }
}
