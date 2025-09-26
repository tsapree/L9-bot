package biz.atomeo.l9.dto;

import biz.atomeo.l9.L9GameService;
import biz.atomeo.l9.L9GameState;
import biz.atomeo.l9.constants.ChatState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SessionDTO {
    Long chatId;
    ChatState chatState;
    L9GameService gameService;
    L9GameState gameState;
}
