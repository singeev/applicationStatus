package com.singeev.applicationstatus;

import com.singeev.applicationstatus.dto.Visa;
import com.singeev.applicationstatus.repository.VisaRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Bot extends TelegramLongPollingBot {
    private final static Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    @Value("${botUserName}")
    private String botUserName;

    @Value("${botToken}")
    private String botToken;

    @Autowired
    private VisaRepository visaRepository;

    private Long masterCharId = 282285200L;

    private boolean isTriggered = false;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            LOGGER.info("User [id={}, firstName={}] sent message: {}",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(),
                    update.getMessage().getText());
            if (update.getMessage().getText().equals("/start")) {
                setMasterCharId(update.getMessage().getChatId());
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Starting update visa status.");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    LOGGER.error("Can't send text message to user [id={}, firstName={}]: {}",
                            update.getMessage().getFrom().getId(),
                            update.getMessage().getFrom().getFirstName(),
                            e.getMessage());
                    e.printStackTrace();
                }
            } else if (update.getMessage().getText().equals("check now")) {
                LOGGER.info("Try to check now...");
                isTriggered = true;
                check();
            }
        }
    }

    private void setMasterCharId(long id) {
        if (masterCharId == null) {
            masterCharId = id;
        }
    }

    @Scheduled(fixedDelay = 3 * 60 * 1000)
    public void check() {
        try {
            URL url = new URL("https://germania.diplo.de/ru-ru/vertretungen/gk-stpe/abholung/1670304");
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();  // ** WRONG: should use "con.getContentType()" instead but it returns something like "text/html; charset=UTF-8" so this value must be parsed to extract the actual encoding
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
            List<String> lastAccepted = getLastAccepted(body);
            LOGGER.info("Successfully parsed web page, got {} numbers.", lastAccepted.size());
            List<String> unseen = saveAndFindNewEntries(lastAccepted);
            LOGGER.info("Compared to saved, found {} new numbers.", lastAccepted.size());
            if (unseen != null && unseen.size() != 0) {
                sendListToUser(unseen);
                saveUnseen(unseen);
            } else if (isTriggered) {
                sendNoNewsMessage();
                isTriggered = false;
            }
        } catch (IOException e) {
            LOGGER.error("Problem: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveUnseen(List<String> unseen) {
        List<Visa> toSave = unseen.stream()
                .map(Visa::new)
                .collect(Collectors.toList());
        visaRepository.saveAll(toSave);
        LOGGER.info("Save {} new numbers.", unseen.size());
    }

    private List<String> getLastAccepted(String page) {
        int start = page.indexOf("Stand: ");
        int end = page.indexOf("<br/></p></div>", start);
        String sub1 = page.substring(start, end);
        int newStart = sub1.indexOf("0005");
        String sub2 = sub1.substring(newStart);
        return Arrays.asList(sub2.split("<br/>"));
    }

    private List<String> saveAndFindNewEntries(List<String> newList) {
        Set<String> previous = visaRepository.findAll()
                .stream()
                .map(Visa::getNumber)
                .collect(Collectors.toCollection(HashSet::new));
        return newList.stream()
                .filter(previous::add)
                .collect(Collectors.toList());
    }

    private void sendListToUser(List<String> unseen) {
        StringBuilder builder = new StringBuilder("New applications accepted:\n");
        unseen.forEach(line -> builder.append(line).append("\n"));
        SendMessage message = new SendMessage()
                .setChatId(masterCharId)
                .setText(builder.toString());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Can't send text message: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendNoNewsMessage() {
        SendMessage message = new SendMessage()
                .setChatId(masterCharId)
                .setText("No new accepted numbers");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Can't send text message: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
