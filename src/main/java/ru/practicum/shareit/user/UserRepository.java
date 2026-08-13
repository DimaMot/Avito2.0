package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long globalId = 0;

    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public boolean existByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }

    public User create(User user) {
        user.setId(getNextId());
        log.info("Пользователю присвоен id {}", user.getId());
        users.put(user.getId(), user);
        return user;
    }

    public User delete(long userId) {
        return users.remove(userId);
    }

    private synchronized long getNextId() {
        return ++globalId;
    }
}
