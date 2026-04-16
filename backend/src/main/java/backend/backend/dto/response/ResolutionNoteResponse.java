package backend.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResolutionNoteResponse {
    private Long id;
    private Long technicianId;
    private String technicianName;
    private String note;
    private LocalDateTime createdAt;
}
