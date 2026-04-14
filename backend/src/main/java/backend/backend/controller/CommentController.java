package backend.backend.controller;

import backend.backend.dto.request.AddCommentRequest;
import backend.backend.dto.response.CommentResponse;
import backend.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long id,
            @RequestBody @Valid AddCommentRequest request) {
        return ResponseEntity.ok(commentService.editComment(id, request.getContent(), 1L));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        // Mocking user 1 and role ADMIN for now
        commentService.deleteComment(id, 1L, "ADMIN");
        return ResponseEntity.noContent().build();
    }
}
