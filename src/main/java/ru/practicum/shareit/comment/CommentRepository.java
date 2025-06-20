package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> getAllCommentsByItemId(int itemId);

    @Query("""
            SELECT c
            FROM Comment AS c
            JOIN FETCH c.item
            JOIN FETCH c.author
            WHERE c.item.user.id = :ownerId
            """)
    List<Comment> findAllCommentsByOwnerWithItemsAndAuthors(int ownerId);
}