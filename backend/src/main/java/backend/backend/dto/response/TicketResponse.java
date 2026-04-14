package backend.backend.dto.response;

import backend.backend.enums.TicketCategory;
import backend.backend.enums.TicketPriority;
import backend.backend.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private String location;
    private String reportedByName;
    private String assignedToName;
    private String rejectionReason;
    private List<AttachmentResponse> attachments;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
