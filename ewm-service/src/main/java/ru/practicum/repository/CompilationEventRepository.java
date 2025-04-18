package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.CompilationEvent;

import java.util.List;

public interface CompilationEventRepository extends JpaRepository<CompilationEvent, Integer> {
    @Query(value = "SELECT c.event_id FROM compilation_event AS c WHERE c.compilation_id = ?1", nativeQuery = true)
    List<Integer> findByCompilationId(int id);

    void deleteByCompilationId(int id);
}
