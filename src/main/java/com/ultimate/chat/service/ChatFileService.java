package com.ultimate.chat.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ixchat.file.storage.location}")
    private String storageLocation;

    public ChatFileService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public ChatAttachmentReq saveFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Only PDF, DOC, DOCX and TXT files are allowed");
        }

        String fileId = UUID.randomUUID().toString();

        // Ensure storage directory exists
        Path storageDir = Paths.get(storageLocation).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);

        // Encrypted file path (named by fileId)
        Path outFile = storageDir.resolve(fileId);

        // Encrypt file stream -> file and get IV (base64)
        try (InputStream in = file.getInputStream()) {
            String ivBase64 = encryptionService.encryptToFile(in, outFile);

            ChatAttachmentReq resp = new ChatAttachmentReq(
                    fileId,
                    originalName,
                    file.getContentType(),
                    file.getSize(),
                    API_BASE + "/api/chat/files/" + fileId + "/download",
                    fileId, // storagePath is fileId for DB lookup
                    (short) 1, // encrypted
                    ivBase64
            );

            // We do NOT store the file bytes in DB; leave fileData null
            resp.setFileData(null);

            return resp;
        } catch (IOException e) {
            // If encryption or write failed, attempt to delete the partially written file
            try {
                Files.deleteIfExists(outFile);
            } catch (Exception ex) {
                // ignore deletion failure, report original error
            }
            throw e;
        }
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index + 1);
    }
}
