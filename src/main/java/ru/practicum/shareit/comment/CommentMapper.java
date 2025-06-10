package ru.practicum.shareit.comment;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Component
public class CommentMapper {

    public Comment dtoToComment(CommentDtoRequest commentDtoRequest, Item item, User user) {
        Comment comment = new Comment();
        comment.setText(commentDtoRequest.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        return comment;
    }

    public CommentDtoResponse commentToDtoResponse(Comment comment) {
        return CommentDtoResponse.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }
}