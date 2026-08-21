package ru.project.avito.item.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.project.avito.item.dto.CommentCreateDto;
import ru.project.avito.item.dto.CommentDto;
import ru.project.avito.item.model.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(source = "comment.author.name", target = "authorName")
    CommentDto toCommentDto(Comment comment);

    Comment toComment(CommentCreateDto create);
}
