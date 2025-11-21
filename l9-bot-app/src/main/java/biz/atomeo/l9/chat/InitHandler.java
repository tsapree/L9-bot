package biz.atomeo.l9.chat;

import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import org.springframework.stereotype.Component;

@Component("INIT_HANDLER")
public class InitHandler implements StateHandler {

    @Override
    public void onCommand(String question, AnswerDTO answer, SessionDTO session) {
        answer.setNewChatState(ChatState.CHOOSE_GAME);
        answer.appendText("Welcome!");
    }
}
