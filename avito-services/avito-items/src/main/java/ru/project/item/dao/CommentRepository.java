package ru.project.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.item.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
