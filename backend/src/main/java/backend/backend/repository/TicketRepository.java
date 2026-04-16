package backend.backend.repository;

import backend.backend.entity.Ticket;
import backend.backend.entity.User;
import backend.backend.enums.TicketCategory;
import backend.backend.enums.TicketPriority;
import backend.backend.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Page<Ticket> findByReportedBy(User reportedBy, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:category IS NULL OR t.category = :category)")
    Page<Ticket> findWithFilters(@Param("status") TicketStatus status,
                                 @Param("priority") TicketPriority priority,
                                 @Param("category") TicketCategory category,
                                 Pageable pageable);
}
