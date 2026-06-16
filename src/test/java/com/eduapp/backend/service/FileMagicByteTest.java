package com.eduapp.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** SEC-7 — uploads are validated by real content signature, not the spoofable content-type. */
class FileMagicByteTest {

    private byte[] pad(byte[] head) {
        byte[] b = new byte[16];
        System.arraycopy(head, 0, b, 0, head.length);
        return b;
    }

    @Test
    void acceptsRealImageSignatures() {
        assertThat(FileStorageService.looksLikeAllowedImage(
                pad(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}))).isTrue(); // JPEG
        assertThat(FileStorageService.looksLikeAllowedImage(
                pad(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}))).isTrue(); // PNG
        assertThat(FileStorageService.looksLikeAllowedImage(
                new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0, 0, 0, 0})).isTrue(); // WEBP
        byte[] heic = new byte[16];
        System.arraycopy(new byte[]{0, 0, 0, 0, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c'}, 0, heic, 0, 12);
        assertThat(FileStorageService.looksLikeAllowedImage(heic)).isTrue(); // HEIC
    }

    @Test
    void rejectsScriptOrRenamedFile() {
        // "<?php ..." / "GIF89-but-actually-script" style payloads with no real image header.
        assertThat(FileStorageService.looksLikeAllowedImage(
                "<script>alert(1)</script>".getBytes())).isFalse();
        assertThat(FileStorageService.looksLikeAllowedImage(new byte[]{1, 2, 3})).isFalse(); // too short
        assertThat(FileStorageService.looksLikeAllowedImage(null)).isFalse();
    }
}
