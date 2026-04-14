package backend.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String filePath;
    private LocalDateTime uploadedAt;
}
