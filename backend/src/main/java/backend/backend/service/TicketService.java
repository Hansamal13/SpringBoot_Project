package backend.backend.service;

import backend.backend.dto.request.CreateTicketRequest;
import backend.backend.dto.request.UpdateStatusRequest;
import backend.backend.dto.response.AttachmentResponse;
import backend.backend.dto.response.TicketResponse;
import backend.backend.entity.Ticket;
import backend.backend.entity.User;
import backend.backend.enums.TicketCategory;
import backend.backend.enums.TicketPriority;
import backend.backend.enums.TicketStatus;
import backend.backend.exception.BadRequestException;
import backend.backend.exception.ResourceNotFoundException;
import backend.backend.repository.TicketRepository;
import backend.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Long userId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        User reporter = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setResourceId(request.getResourceId());
        ticket.setLocation(request.getLocation());
        ticket.setContactName(request.getContactName());
        ticket.setContactEmail(request.getContactEmail());
        ticket.setContactPhone(request.getContactPhone());
        ticket.setReportedBy(reporter);
        ticket.setStatus(TicketStatus.OPEN);

        return mapToResponse(ticketRepository.save(ticket));
    }

    public TicketResponse getTicketById(Long id) {
        Objects.requireNonNull(id, "Ticket ID must not be null");
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return mapToResponse(ticket);
    }

    public Page<TicketResponse> getTickets(TicketStatus status, TicketPriority priority, TicketCategory category, Pageable pageable) {
        return ticketRepository.findWithFilters(status, priority, category, pageable)
            .map(this::mapToResponse);
    }

    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateStatusRequest request, Long actorId) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null");
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        validateStatusTransition(ticket.getStatus(), request.getNewStatus());

        if (request.getNewStatus() == TicketStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new BadRequestException("Rejection reason is required");
            }
            ticket.setRejectionReason(request.getRejectionReason());
        }

        ticket.setStatus(request.getNewStatus());
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignTechnician(Long ticketId, Long technicianId) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null");
        Objects.requireNonNull(technicianId, "Technician ID must not be null");
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User technician = userRepository.findById(technicianId)
            .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        ticket.setAssignedTo(technician);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return mapToResponse(ticketRepository.save(ticket));
    }

    private void validateStatusTransition(TicketStatus current, TicketStatus next) {
        Map<TicketStatus, List<TicketStatus>> allowed = Map.of(
            TicketStatus.OPEN,        List.of(TicketStatus.IN_PROGRESS, TicketStatus.REJECTED),
            TicketStatus.IN_PROGRESS, List.of(TicketStatus.RESOLVED, TicketStatus.REJECTED),
            TicketStatus.RESOLVED,    List.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
            TicketStatus.REJECTED,    List.of(),
            TicketStatus.CLOSED,      List.of()
        );

        List<TicketStatus> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new BadRequestException("Cannot transition from " + current + " to " + next);
        }
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setCategory(ticket.getCategory());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setLocation(ticket.getLocation());
        response.setReportedByName(ticket.getReportedBy().getName());
        if (ticket.getAssignedTo() != null) {
            response.setAssignedToName(ticket.getAssignedTo().getName());
        }
        response.setRejectionReason(ticket.getRejectionReason());
        
        if (ticket.getAttachments() != null) {
            response.setAttachments(ticket.getAttachments().stream()
                .map(a -> {
                    AttachmentResponse ar = new AttachmentResponse();
                    ar.setId(a.getId());
                    ar.setFileName(a.getFileName());
                    ar.setFileType(a.getFileType());
                    ar.setFilePath(a.getFilePath());
                    ar.setUploadedAt(a.getUploadedAt());
                    return ar;
                }).collect(Collectors.toList()));
        }
        
        response.setCommentCount(ticket.getComments() != null ? ticket.getComments().size() : 0);
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        return response;
    }

    @Transactional
    public void deleteTicket(Long id) {
        Objects.requireNonNull(id, "Ticket ID must not be null");
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket not found");
        }
        ticketRepository.deleteById(id);
    }
}
