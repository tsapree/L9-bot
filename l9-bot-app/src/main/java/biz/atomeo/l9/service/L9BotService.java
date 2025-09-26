package biz.atomeo.l9.service;

import biz.atomeo.l9.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
public class L9BotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final String token;

    private final ChatService chatService;

    public L9BotService(
            @Value("${botToken}")
            String token,
            ChatService chatService) {
        this.token = token;
        telegramClient = new OkHttpTelegramClient(token);

        this.chatService = chatService;
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

            String generatedMessage = chatService.generateAnswer(chatId, command);

            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chatId)
                    .text(generatedMessage)
                    .build();

            try {
                log.info("Response to chatId [{}]=[{}]", chatId, generatedMessage);
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: " + botSession.isRunning());
    }
}
