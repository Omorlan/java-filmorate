package ru.yandex.practicum.filmorate.model.event;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    @NotNull
    private Long timestamp;
    @NotNull
    private Long userId;
    @NotNull
    private EventType eventType;
    @NotNull
    private OperationType operation;
    @NotNull
    private Long eventId;
    @NotNull
    private Long entityId;
}
