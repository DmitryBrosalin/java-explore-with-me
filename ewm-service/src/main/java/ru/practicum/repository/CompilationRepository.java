package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Compilation;

import java.util.Arrays;
import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Integer> {
    @Query(value = "SELECT * FROM compilations AS c WHERE c.id > ?1 LIMIT ?2", nativeQuery = true)
    List<Compilation> findAllLimit(int from, int size);

    @Query(value = "SELECT * FROM compilations AS c WHERE c.pinned = ?1 AND  c.id > ?2 LIMIT ?3", nativeQuery = true)
    List<Compilation> findAllPinnedLimit(boolean pinned, int from, int size);
}
