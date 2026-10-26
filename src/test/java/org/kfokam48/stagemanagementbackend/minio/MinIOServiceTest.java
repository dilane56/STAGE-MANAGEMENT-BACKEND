package org.kfokam48.stagemanagementbackend.minio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class MinIOServiceTest {

    private MinIOService minIOService;

    @BeforeEach
    void setUp() {
        minIOService = new MinIOService("http://localhost:9000", "access", "secret", "us-east-1");
        ReflectionTestUtils.setField(minIOService, "bucketName", "stage-management");
    }

    @Test
    void objectNameIsKeptAsIs() {
        assertEquals("abc.pdf", minIOService.resolveObjectName("abc.pdf"));
    }

    @Test
    void legacyPresignedUrlIsConvertedToObjectName() {
        String legacy = "http://localhost:9000/stage-management/1700000000000_mon%20cv.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=604800";
        assertEquals("1700000000000_mon cv.pdf", minIOService.resolveObjectName(legacy));
    }

    @Test
    void emptyValueGivesNull() {
        assertNull(minIOService.resolveObjectName(null));
        assertNull(minIOService.resolveObjectName(" "));
    }

    @Test
    void nonPdfFileIsRejectedBeforeUpload() {
        MockMultipartFile fakePdf = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "pas un pdf".getBytes());
        assertThrows(IllegalArgumentException.class, () -> minIOService.uploadFile(fakePdf));

        MockMultipartFile docx = new MockMultipartFile("cv", "cv.docx", "application/octet-stream", "%PDF-1.4".getBytes());
        assertThrows(IllegalArgumentException.class, () -> minIOService.uploadFile(docx));

        MockMultipartFile empty = new MockMultipartFile("cv", "cv.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> minIOService.uploadFile(empty));
    }
}
