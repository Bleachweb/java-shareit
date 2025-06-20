package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findAllItemsByUserOrderByIdAsc(User user);

    @Query("""
            SELECT i
            FROM Item AS i
            WHERE i.available IS TRUE
            AND (i.name ILIKE %:text%
                 OR i.description ILIKE %:text%)
            """)
    List<Item> search(String text);

    @Query("""
            SELECT i
            FROM Item AS i
            LEFT JOIN FETCH i.user
            WHERE i.user.id = :userId
            ORDER BY i.id ASC
            """)
    List<Item> findAllByUserIdWithOwner(int userId);
}