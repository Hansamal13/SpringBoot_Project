package backend.backend.repository;

import backend.backend.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    long countByTicketId(Long ticketId);
}
