package com.ultimate.chat.service;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ultimate.chat.util.EncryptionService;

import com.ultimate.chat.dto.ChatAttachmentReq;

@Service
public class ChatFileService {

	private final String API_BASE = "/onyx-ix/main";

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "doc", "docx", "txt");

    private final EncryptionService encryptionService;

    public ChatFileService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public ChatAttachmentReq saveFile(
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is empty"
            );
        }

        String originalName =
                file.getOriginalFilename();

        if (originalName == null ||
                originalName.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid file name"
            );
        }

        String extension =
                getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(
                extension.toLowerCase())) {

            throw new IllegalArgumentException(
                    "Only PDF, DOC, DOCX and TXT files are allowed"
            );
        }

        String fileId =
                UUID.randomUUID().toString();

        // Encrypt file bytes
        byte[] fileBytes = file.getBytes();
        EncryptionService.ByteEncryptionResult encResult = encryptionService.encryptBytes(fileBytes);
        
        // Encode encrypted bytes as base64 string for TEXT storage
        String encryptedBase64 = Base64.getEncoder().encodeToString(encResult.encryptedData);

        ChatAttachmentReq resp = new ChatAttachmentReq(
                fileId,
                originalName,
                file.getContentType(),
                file.getSize(),
                API_BASE + "/api/chat/files/" + fileId + "/download",
                fileId, // storagePath is now fileId for database lookup
                (short)1, // encrypted
                encResult.ivBase64
        );
        
        resp.setFileData(encryptedBase64);

        return resp;
    }

    private String getExtension(
            String fileName) {

        int index =
                fileName.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return fileName
                .substring(index + 1);
    }
}
