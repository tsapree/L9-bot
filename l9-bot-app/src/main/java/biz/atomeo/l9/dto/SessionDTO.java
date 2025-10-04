package biz.atomeo.l9.dto;

import biz.atomeo.l9.bot.L9GameService;
import biz.atomeo.l9.bot.L9GameState;
import biz.atomeo.l9.constants.ChatState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTO {
    Long chatId;
    OffsetDateTime created;
    OffsetDateTime updated;
    ChatState chatState;
    L9GameState gameState;
    @JsonIgnore
    L9GameService gameService;
    Long version;
    @JsonIgnore
    boolean lock;
}
