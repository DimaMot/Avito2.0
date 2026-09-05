package ru.project.item.dto.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.project.item.dto.CommentCreateDto;
import ru.project.item.dto.CommentDto;
import ru.project.item.model.Comment;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(target = "authorName", expression = "java(userNames != null ? userNames.get(comment.getAuthorId()) : null)")
    CommentDto toCommentDto(Comment comment, @Context Map<Long, String> userNames);

    List<CommentDto> toCommentDtoList(List<Comment> comments, @Context Map<Long, String> userNames);

    Comment toComment(CommentCreateDto create);
}
