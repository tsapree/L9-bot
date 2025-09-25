package biz.atomeo.l9.service;

import biz.atomeo.l9.L9GameService;
import biz.atomeo.l9.L9GameState;
import biz.atomeo.l9.L9Request;
import biz.atomeo.l9.L9Response;
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

    private final SessionProvider sessionProvider;
    private final L9BotStateProvider l9BotState;

    //TODO: is commands need any symbols more?
    private final Pattern USER_COMMAND_PATTERN = Pattern.compile("^[0-9A-Za-z .,]{1,100}$");

    public String generateAnswer(Long chatId, String command) {
        if (!l9BotState.isBotActive()) return "Sorry, application is unavailable right now.";

        try {
            SessionDTO session = sessionProvider.getSession(chatId);

            L9GameState l9GameState = session.getGameState();
            //TODO: to save memory on each user need to refactor L9GameService
            L9GameService l9GameService = session.getGameService();

            String response = doL9Stuff(command, l9GameState, l9GameService);

            sessionProvider.updateSession(chatId, session);
            return response;
        } catch (L9Exception e) {
            log.error("Error generating message:", e);
            return "Sorry, something went wrong.";
        }
    }

    private String doL9Stuff(String userCommand,
                             L9GameState l9GameState,
                             L9GameService l9GameService ) throws L9Exception {

        validateCommand(userCommand);

        L9Request request = L9Request.builder()
                .command(userCommand)
                .build();

        L9Response response = l9GameService.doStep(request, l9GameState);

        if (StringUtils.isBlank(response.getMessage()))
            throw new L9Exception("Generated empty message. Something goes wrong!");
        return response.getMessage();
    }

    private void validateCommand(String command) throws L9Exception {
        if (!USER_COMMAND_PATTERN.matcher(command).matches()) {
            throw new L9Exception("Incorrect user command.");
        }
    }
}
