package com.singeev.applicationstatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
public class BotStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotStarter.class);

    private Bot bot;

    @Autowired
    public BotStarter(Bot bot) {
        this.bot = bot;
    }

    @PostConstruct
    private void init() {

        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(bot);
            LOGGER.info("Bot started and registered successfully!");
        } catch (TelegramApiException e) {
            LOGGER.error("Can't start bot: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
