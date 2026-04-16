package backend.backend.repository;

import backend.backend.entity.ResolutionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResolutionNoteRepository extends JpaRepository<ResolutionNote, Long> {
    List<ResolutionNote> findByTicketId(Long ticketId);
}
