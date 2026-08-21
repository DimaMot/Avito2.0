package ru.project.avito.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.avito.item.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
