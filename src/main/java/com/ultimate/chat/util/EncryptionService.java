package com.ultimate.chat.util;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits
    private static final int TAG_LENGTH = 128; // bits

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public EncryptionService(SecretKey fileEncryptionKey) {
        this.key = fileEncryptionKey;
    }

    public String encryptToFile(InputStream in, Path out) throws IOException {
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);

        try (OutputStream fos = Files.newOutputStream(out)) {
            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                byte[] buffer = new byte[8192];
                int r;
                while ((r = in.read(buffer)) != -1) {
                    cos.write(buffer, 0, r);
                }
            }
        } catch (Exception e) {
            throw new IOException("Encryption failed", e);
        }

        return Base64.getEncoder().encodeToString(iv);
    }

    public byte[] decryptToBytes(Path encryptedFile, String base64Iv) throws IOException {
        byte[] iv = Base64.getDecoder().decode(base64Iv);

        try (InputStream fis = Files.newInputStream(encryptedFile)) {
            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int r;
                while ((r = cis.read(buffer)) != -1) {
                    baos.write(buffer, 0, r);
                }

                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new IOException("Decryption failed", e);
        }
    }

    public static class StringEncryptionResult {
        public final String cipherTextBase64;
        public final String ivBase64;

        public StringEncryptionResult(String cipherTextBase64, String ivBase64) {
            this.cipherTextBase64 = cipherTextBase64;
            this.ivBase64 = ivBase64;
        }
    }

    public static class ByteEncryptionResult {
        public final byte[] encryptedData;
        public final String ivBase64;

        public ByteEncryptionResult(byte[] encryptedData, String ivBase64) {
            this.encryptedData = encryptedData;
            this.ivBase64 = ivBase64;
        }
    }

    public ByteEncryptionResult encryptBytes(byte[] data) throws IOException {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encryptedData = cipher.doFinal(data);

            return new ByteEncryptionResult(encryptedData, Base64.getEncoder().encodeToString(iv));
        } catch (Exception e) {
            throw new IOException("Bytes encryption failed", e);
        }
    }

    public byte[] decryptBytes(byte[] encryptedData, String ivBase64) throws IOException {
        try {
            byte[] iv = Base64.getDecoder().decode(ivBase64);

            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new IOException("Bytes decryption failed", e);
        }
    }

    public StringEncryptionResult encryptString(String plain) throws IOException {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherBytes = cipher.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return new StringEncryptionResult(Base64.getEncoder().encodeToString(cipherBytes), Base64.getEncoder().encodeToString(iv));
        } catch (Exception e) {
            throw new IOException("String encryption failed", e);
        }
    }

    public String decryptString(String cipherTextBase64, String ivBase64) throws IOException {
        try {
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] cipherBytes = Base64.getDecoder().decode(cipherTextBase64);

            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IOException("String decryption failed", e);
        }
    }
}
