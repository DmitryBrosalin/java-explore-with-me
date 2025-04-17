package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    @Query(value = "SELECT * FROM events AS e WHERE e.initiator_id = ?1 " +
            "AND e.id > ?2 ORDER BY e.id LIMIT ?3", nativeQuery = true)
    List<Event> findByInitiatorIdFromLimit(int userId, int from, int size);
    }
