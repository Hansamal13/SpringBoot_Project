package backend.backend.dto.request;

import backend.backend.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private TicketStatus newStatus;

    private String rejectionReason; // required only when newStatus = REJECTED
}
