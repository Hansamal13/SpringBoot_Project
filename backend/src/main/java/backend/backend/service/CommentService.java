package backend.backend.service;

import backend.backend.dto.response.CommentResponse;
import backend.backend.entity.Ticket;
import backend.backend.entity.TicketComment;
import backend.backend.exception.ForbiddenException;
import backend.backend.exception.ResourceNotFoundException;
import backend.backend.repository.TicketCommentRepository;
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
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getComments(Long ticketId) {
        return commentRepository.findByTicketIdAndIsDeletedFalse(ticketId)
            .stream().map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, String content, Long authorId) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null");
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setContent(content);
        comment.setAuthorId(authorId);
        
        return mapToResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse editComment(Long commentId, String newContent, Long requesterId) {
        Objects.requireNonNull(commentId, "Comment ID must not be null");
        TicketComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getAuthorId().equals(requesterId)) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        comment.setContent(newContent);
        return mapToResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId, String role) {
        Objects.requireNonNull(commentId, "Comment ID must not be null");
        TicketComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isOwner = comment.getAuthorId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(role);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Not authorized to delete this comment");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private CommentResponse mapToResponse(TicketComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setAuthorId(comment.getAuthorId());
        // In a real app, you'd fetch the name from the user service/repository
        Long authorId = comment.getAuthorId();
        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(u -> response.setAuthorName(u.getName()));
        }
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
