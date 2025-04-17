package ru.practicum.model;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventSpecification {
    public Specification<Event> buildPublicSpecification(String text,
                                                   List<Integer> categories,
                                                   Boolean paid,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd) {
        return withText(text)
                .and(withCategories(categories))
                .and(withTime(rangeStart, rangeEnd))
                .and(withPaid(paid))
                .and(withStates(List.of(String.valueOf(State.PUBLISHED))));

    }

    public Specification<Event> buildAdminSpecification(List<Integer> users,
                                                        List<String> states,
                                                        List<Integer> categories,
                                                        LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd) {
        return withUsers(users)
                .and(withStates(states))
                .and(withCategories(categories))
                .and(withTime(rangeStart, rangeEnd));

    }

    private Specification<Event> withText(String text) {
        return (root, query, cb) -> text == null || text.isBlank() ? cb.conjunction() :
                cb.or(cb.like(cb.lower(root.get("annotation")), text),
                cb.like(cb.lower(root.get("description")), text));
    }

    private Specification<Event> withCategories(List<Integer> categories) {
        return (root, query, cb) -> categories == null || categories.isEmpty() ? cb.conjunction() :
                root.get("category").get("id").in(categories);
    }

    private Specification<Event> withTime(LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd) {
        return (root, query, cb) -> rangeStart == null && rangeEnd == null ?
                cb.greaterThan(root.get("eventDate"), LocalDateTime.now()) :
                cb.and(cb.greaterThan(root.get("eventDate"), rangeStart),
                        cb.lessThan(root.get("eventDate"), rangeEnd));
    }

    private Specification<Event> withPaid(Boolean paid) {
        return (root, query, cb) -> paid == null ? cb.conjunction() : cb.equal(root.get("paid"), paid);
    }

    private Specification<Event> withStates(List<String> states) {
        return (root, query, cb) -> states == null || states.isEmpty() ? cb.conjunction() :
                root.get("state").in(states);
    }

    private Specification<Event> withUsers(List<Integer> users) {
        return (root, query, cb) -> users == null || users.isEmpty() ? cb.conjunction() :
                root.get("initiator").get("id").in(users);
    }
}
