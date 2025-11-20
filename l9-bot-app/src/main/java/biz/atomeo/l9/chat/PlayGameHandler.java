package biz.atomeo.l9.chat;

import biz.atomeo.l9.config.L9AppProperties;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.service.L9ReplyService;
import biz.atomeo.l9.utils.FileIOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("PLAYING_GAME_HANDLER")
@RequiredArgsConstructor
public class PlayGameHandler implements StateHandler {

    private final L9ReplyService l9ReplyService;
    private final L9AppProperties l9AppProperties;

    @Override
    public void onCommand(String question, AnswerDTO answer, SessionDTO session) {
        if (parseHelperCommands(question, answer, session)) return;

        AnswerDTO answerDTO = l9ReplyService.generateAnswer(session, question);
        answer.append(answerDTO);
        if (ChatState.STOPPED_GAME.equals(answer.getNewChatState())) {
            answer.setNewChatState(ChatState.CHOOSE_GAME);
        }
    }

    private boolean parseHelperCommands(String question, AnswerDTO answer, SessionDTO session) {
        switch (question.trim()) {
            case "#howtoplay":
                String help = FileIOUtils.loadTextFromResource("articles/how_to_play.md", "Error reading 'how to play' article.");
                answer.appendText(help);
                answer.appendText("What next?");
                return true;
            case "#about":
                answer.appendText(String.format("Please visit %s for more information about this game.",
                        l9AppProperties.getGames().get(session.getGameState().getL9game().name()).about()));
                return true;
        }
        return false;
    }
}
