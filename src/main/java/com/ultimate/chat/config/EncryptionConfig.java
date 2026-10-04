package com.ultimate.chat.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class EncryptionConfig {

    private static final Logger log = LoggerFactory.getLogger(EncryptionConfig.class);

    // Expect a Base64-encoded 32-byte (256-bit) key in environment or properties
    @Value("${ixchat.encryption.key:}")
    private String base64Key;

    @Bean
    public SecretKey fileEncryptionKey() {
        try {
            String key = base64Key;
            if ((key == null || key.isBlank()) && System.getenv("IXCHAT_FILE_ENC_KEY") != null) {
                key = System.getenv("IXCHAT_FILE_ENC_KEY");
            }

            if (key == null || key.isBlank()) {
                // Development fallback: generate ephemeral 256-bit key and log it for developer convenience
                byte[] generated = new byte[32];
                new SecureRandom().nextBytes(generated);
                String generatedBase64 = Base64.getEncoder().encodeToString(generated);
                log.warn("No encryption key configured. Generating ephemeral AES-256 key for development.\nSet IXCHAT_FILE_ENC_KEY or ixchat.encryption.key with this value to persist across restarts:\n{}", generatedBase64);
                key = generatedBase64;
            }

            byte[] decoded = Base64.getDecoder().decode(key);

            if (!(decoded.length == 16 || decoded.length == 24 || decoded.length == 32)) {
                log.warn("Decoded encryption key has unexpected length ({} bytes). AES expects 16, 24 or 32 bytes.", decoded.length);
            }

            return new SecretKeySpec(decoded, "AES");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create file encryption key", ex);
        }
    }
}
