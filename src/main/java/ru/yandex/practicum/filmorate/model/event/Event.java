package ru.yandex.practicum.filmorate.model.event;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    @NotNull
    Long timestamp;
    @NotNull
    Long userId;
    @NotNull
    EventType eventType;
    @NotNull
    OperationType operation;
    @NotNull
    Long eventId;
    @NotNull
    Long entityId;
}
