package backend.backend.controller;

import backend.backend.dto.request.*;
import backend.backend.dto.response.*;
import backend.backend.enums.TicketCategory;
import backend.backend.enums.TicketPriority;
import backend.backend.enums.TicketStatus;
import backend.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AttachmentService attachmentService;
    private final CommentService commentService;
    private final ResolutionNoteService resolutionNoteService;

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            Pageable pageable) {
        // In real app, extracting user from SecurityContext
        // For now, simple return
        return ResponseEntity.ok(ticketService.getTickets(status, priority, category, pageable));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        // Mocking user ID 1 for now until security is fully integrated
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ticketService.createTicket(request, 1L));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request, 1L));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(attachmentService.uploadAttachment(id, file, 1L));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getComments(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @RequestBody @Valid AddCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.addComment(id, request.getContent(), 1L));
    }

    @GetMapping("/{id}/resolution-notes")
    public ResponseEntity<List<ResolutionNoteResponse>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(resolutionNoteService.getNotes(id));
    }

    @PostMapping("/{id}/resolution-notes")
    public ResponseEntity<ResolutionNoteResponse> addNote(
            @PathVariable Long id,
            @RequestBody @Valid AddNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(resolutionNoteService.addNote(id, request.getNote(), 1L));
    }
}
