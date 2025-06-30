package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestParams {
    private String state;
    private Integer from;
    private Integer size;

    public Map<String, Object> toMap() {
        return Map.of(
                "state", state,
                "from", from,
                "size", size
        );
    }
}