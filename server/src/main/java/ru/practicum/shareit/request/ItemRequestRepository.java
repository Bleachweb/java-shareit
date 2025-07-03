package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    @Query("""
            SELECT DISTINCT ir
            FROM ItemRequest AS ir
            JOIN FETCH ir.requester
            WHERE ir.requester.id = ?1
            """)
    List<ItemRequest> findAllByRequesterIdWithUser(int userId);
}