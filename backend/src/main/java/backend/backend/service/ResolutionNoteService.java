package backend.backend.service;

import backend.backend.dto.response.ResolutionNoteResponse;
import backend.backend.entity.ResolutionNote;
import backend.backend.entity.Ticket;
import backend.backend.exception.ResourceNotFoundException;
import backend.backend.repository.ResolutionNoteRepository;
import backend.backend.repository.TicketRepository;
import backend.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResolutionNoteService {

    private final ResolutionNoteRepository noteRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public List<ResolutionNoteResponse> getNotes(Long ticketId) {
        return noteRepository.findByTicketId(ticketId)
            .stream().map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ResolutionNoteResponse addNote(Long ticketId, String noteText, Long technicianId) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null");
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ResolutionNote note = new ResolutionNote();
        note.setTicket(ticket);
        note.setNote(noteText);
        note.setTechnicianId(technicianId);

        return mapToResponse(noteRepository.save(note));
    }

    private ResolutionNoteResponse mapToResponse(ResolutionNote note) {
        ResolutionNoteResponse response = new ResolutionNoteResponse();
        response.setId(note.getId());
        response.setTechnicianId(note.getTechnicianId());
        Long technicianId = note.getTechnicianId();
        if (technicianId != null) {
            userRepository.findById(technicianId).ifPresent(u -> response.setTechnicianName(u.getName()));
        }
        response.setNote(note.getNote());
        response.setCreatedAt(note.getCreatedAt());
        return response;
    }
}
