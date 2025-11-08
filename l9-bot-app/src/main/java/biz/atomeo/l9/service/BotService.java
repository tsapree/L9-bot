package biz.atomeo.l9.service;

import biz.atomeo.l9.bot.L9BotConnector;
import biz.atomeo.l9.dto.AnswerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;

@Service
@Slf4j
public class BotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final String token;

    private final ChatService chatService;
    private final TgInputFileProvider inputFileProvider;

    public BotService(
            @Value("${botToken}")
            String token,
            ChatService chatService,
            TgInputFileProvider inputFileProvider
    ) {
        this.token = token;
        this.chatService = chatService;
        this.inputFileProvider = inputFileProvider;

        telegramClient = new OkHttpTelegramClient(token);
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            log.info("Request from chatId [{}]=[{}]", chatId, command);

            AnswerDTO generatedMessage = chatService.generateAnswer(chatId, command);

            //send picture(s) in separate msg
            if (generatedMessage.getPicturesFilenames() != null) {
                for (String filename : generatedMessage.getPicturesFilenames()) {
                    if (L9BotConnector.GIF_ANIMATION) {
                        sendMsg(chatId, SendAnimation
                                .builder()
                                .chatId(chatId)
                                .animation(new InputFile(new File(filename)))
                                .build());
                        //TODO: to cache animations after solving problem with restart gifs
                    } else {
                        InputFile file = inputFileProvider.getInputFile(filename);
                        Message message = sendMsg(chatId, SendPhoto
                                .builder()
                                .chatId(chatId)
                                .photo(file)
                                .build());
                        inputFileProvider.cachePhotoFileId(file, filename, message);
                    }
                }
            }

            //send text
            sendMsg(chatId, SendMessage
                    .builder()
                    .chatId(chatId)
                    .text(generatedMessage.getAnswerText())
                    .build());
        }
    }

    private <T> Message sendMsg(long chatId, T msg) {
        try {
            if (msg instanceof SendAnimation) {
                log.debug("SendAnimation to chatId [{}]", chatId);
                return telegramClient.execute((SendAnimation)msg);
            } else if (msg instanceof SendPhoto) {
                log.debug("SendPhoto to chatId [{}]: {}", chatId, ((SendPhoto) msg).getPhoto().getAttachName());
                return telegramClient.execute((SendPhoto)msg);
            } else if (msg instanceof SendMessage) {
                log.info("SendMessage to chatId [{}]: {}", chatId, ((SendMessage) msg).getText());
                return telegramClient.execute((SendMessage)msg);
            }
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
        return null;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: " + botSession.isRunning());
    }
}
