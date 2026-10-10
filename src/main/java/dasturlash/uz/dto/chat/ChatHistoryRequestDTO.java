package dasturlash.uz.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatHistoryRequestDTO {
    private LocalDateTime lmCreatedDate;
}
