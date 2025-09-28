package biz.atomeo.l9.service;

import biz.atomeo.l9.bot.L9GameService;
import biz.atomeo.l9.bot.L9GameState;
import biz.atomeo.l9.bot.dto.L9Request;
import biz.atomeo.l9.bot.dto.L9Response;
import biz.atomeo.l9.constants.ChatState;
import biz.atomeo.l9.constants.L9Phase;
import biz.atomeo.l9.dto.AnswerDTO;
import biz.atomeo.l9.dto.SessionDTO;
import biz.atomeo.l9.error.L9Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class L9ReplyService {

    //TODO: is commands need any symbols more?
    private final Pattern USER_COMMAND_PATTERN = Pattern.compile("^[0-9A-Za-z .,#]{1,100}$");

    public AnswerDTO generateAnswer(SessionDTO session, String command) {
        try {
            L9GameState l9GameState = session.getGameState();
            //TODO: to save memory on each user need to refactor L9GameService
            L9GameService l9GameService = session.getGameService();

            AnswerDTO response = doL9Stuff(command, l9GameState, l9GameService);

            return response;
        } catch (L9Exception e) {
            log.error("Error generating message:", e);
            return AnswerDTO.builder()
                    .answerText("Sorry, something went wrong.")
                    .build();
        }
    }

    private AnswerDTO doL9Stuff(String userCommand,
                             L9GameState l9GameState,
                             L9GameService l9GameService) throws L9Exception {

        validateCommand(userCommand);

        L9Request request = L9Request.builder()
                .command(userCommand)
                .build();

        L9Response response = l9GameService.doStep(request, l9GameState);

        if (StringUtils.isBlank(response.getMessage()))
            throw new L9Exception("Generated empty message. Something goes wrong!");

        return AnswerDTO.builder()
                .chatState(response.getPhase()!=L9Phase.STOPPED ?
                        ChatState.PLAYING_GAME : ChatState.STOPPED_GAME)
                .answerText(response.getMessage())
                .picturesFilenames(response.getPictures())
                .build();
    }

    private void validateCommand(String command) throws L9Exception {
        if (!USER_COMMAND_PATTERN.matcher(command).matches()) {
            throw new L9Exception("Incorrect user command.");
        }
    }
}
