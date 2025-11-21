package biz.atomeo.l9.chat;

import biz.atomeo.l9.config.L9AppProperties;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.GameInfo;
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
        GameInfo gameInfo = l9AppProperties.getGames().get(session.getGameState().getL9game().name());
        switch (question.trim().toLowerCase()) {
            case "/howtoplay":
                String help = FileIOUtils.loadTextFromResource("articles/how_to_play.md", "Error reading 'how to play' article.");
                answer.appendText(help);
                answer.appendText("What next?");
                return true;
            case "/about":
                answer.appendText(String.format("You are playing the game '%s'.\n" +
                                "Please visit %s for more information about this game.\n",
                        gameInfo.name(),
                        gameInfo.about()));
                answer.appendText("What next?");
                return true;
            case "/help":
                answer.appendText("""
                        You can use these commands while play the game:\s
                        /about - get information about game, hints, walkthrough from l9memorial site\s
                        /help - this info\s
                        /howtoplay - how to play\s
                        /quit - to quit the game\s
                        """);
                answer.appendText("What next?");
                return true;
            case "/quit":
                answer.appendText(String.format("You quit the game '%s'.\n",
                        gameInfo.name()));
                answer.setNewChatState(ChatState.CHOOSE_GAME);
                return true;
        }
        return false;
    }
}
