package org.kfokam48.stagemanagementbackend.minio;



import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Stockage des CV dans MinIO.
 * <p>
 * Auparavant, une URL pré-signée valable 7 jours était stockée en base : passé ce délai,
 * les CV devenaient inaccessibles. Désormais on stocke le <b>nom de l'objet</b> et on génère
 * une URL pré-signée courte à chaque lecture. Les anciennes valeurs (URL complètes) restent
 * compatibles grâce à {@link #resolveObjectName(String)}.
 */
@Service
public class MinIOService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOService.class);
    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F'};

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.presigned-url.expiry-minutes:60}")
    private int presignedUrlExpiryMinutes;

    public MinIOService(@Value("${minio.url}") String minioUrl,
                        @Value("${minio.access-key}") String accessKey,
                        @Value("${minio.secret-key}") String secretKey,
                        @Value("${minio.region:us-east-1}") String region) {
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                // Région explicite : évite un appel réseau à chaque génération d'URL pré-signée
                .region(region)
                .build();
    }

    /**
     * Téléverse un CV (PDF uniquement) et renvoie le nom de l'objet à stocker en base.
     */
    public String uploadFile(MultipartFile file) throws Exception {
        validatePdf(file);
        String objectName = UUID.randomUUID() + ".pdf";

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType("application/pdf")
                    .build());
        }
        return objectName;
    }

    /** URL pré-signée de courte durée, ou null si le fichier est absent / MinIO indisponible. */
    public String getPresignedUrl(String storedValue) {
        String objectName = resolveObjectName(storedValue);
        if (objectName == null) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(presignedUrlExpiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            logger.warn("Impossible de générer l'URL du fichier {} : {}", objectName, e.getMessage());
            return null;
        }
    }

    /** Lit le contenu d'un fichier directement depuis MinIO (sans requête HTTP vers une URL stockée). */
    public byte[] downloadFile(String storedValue) throws Exception {
        String objectName = resolveObjectName(storedValue);
        if (objectName == null) {
            throw new IllegalArgumentException("Aucun fichier associé");
        }
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build())) {
            return stream.readAllBytes();
        }
    }

    /**
     * Nom de l'objet MinIO à partir de la valeur stockée : soit déjà un nom d'objet,
     * soit une ancienne URL pré-signée "http(s)://hote/bucket/objet?X-Amz-...".
     */
    String resolveObjectName(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return null;
        }
        String value = storedValue.replace("\\", "/");
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return value;
        }
        String path = URI.create(value).getRawPath();
        String prefix = "/" + bucketName + "/";
        String objectName = path.startsWith(prefix) ? path.substring(prefix.length()) : path.substring(path.lastIndexOf('/') + 1);
        return URLDecoder.decode(objectName, StandardCharsets.UTF_8);
    }

    private void validatePdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le CV est obligatoire");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!originalName.endsWith(".pdf")) {
            throw new IllegalArgumentException("Le CV doit être un fichier PDF");
        }
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(PDF_MAGIC.length);
            for (int i = 0; i < PDF_MAGIC.length; i++) {
                if (header.length <= i || header[i] != PDF_MAGIC[i]) {
                    throw new IllegalArgumentException("Le fichier envoyé n'est pas un PDF valide");
                }
            }
        }
    }
}
