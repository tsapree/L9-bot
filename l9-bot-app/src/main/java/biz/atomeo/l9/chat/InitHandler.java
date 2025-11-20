package biz.atomeo.l9.chat;

import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("INIT_HANDLER")
public class InitHandler implements StateHandler {

    @Value("${l9.version}")
    private String botVersion;

    @Override
    public void onEnterState(AnswerDTO answer, SessionDTO session) {
        answer.setNewChatState(ChatState.CHOOSE_GAME);
        answer.appendText(String.format("""
                       Welcome to L9 Games Bot %s!\s
                       \s
                       """, botVersion));
    }
}
