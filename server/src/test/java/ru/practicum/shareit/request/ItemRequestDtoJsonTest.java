package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDtoJsonTest {
    private final JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDto() throws Exception {
        User requester = new User();
        requester.setId(1);
        requester.setName("requester");
        requester.setEmail("requester@itemRequest.com");

        ItemRequestDto request = new ItemRequestDto(
                1,
                "description",
                "11.03.2025 19:55:00",
                requester,
                null);

        JsonContent<ItemRequestDto> result = json.write(request);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("11.03.2025 19:55:00");
        assertThat(result).extractingJsonPathNumberValue("$.requester.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.requester.name").isEqualTo("requester");
        assertThat(result).extractingJsonPathStringValue("$.requester.email").isEqualTo("requester@itemRequest.com");
        assertThat(result).extractingJsonPathStringValue("$.items").isEqualTo(null);
    }
}