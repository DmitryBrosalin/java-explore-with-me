package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
    Optional<Request> findByEventIdAndRequesterId(int eventId, int requesterId);

    @Query(value = "SELECT COUNT(r.id) FROM requests r WHERE r.event_id = ?1 AND r.status LIKE 'CONFIRMED'", nativeQuery = true)
    Long countConfirmedRequestsForEvent(int eventId);

    List<Request> findByRequesterId(int userId);

    List<Request> findByEventId(int eventId);
}
