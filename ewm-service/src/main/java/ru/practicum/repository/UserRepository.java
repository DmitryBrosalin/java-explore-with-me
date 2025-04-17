package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.id > ?1 ORDER BY u.id LIMIT ?2")
    List<User> findUsers(int from, int size);

    @Query("SELECT u FROM User u WHERE u.id IN ?1 AND u.id > ?2 ORDER BY u.id LIMIT ?3")
    List<User> findUsersByIds(List<Integer> ids, int from, int size);

    Optional<User> findByEmail(String email);
}
