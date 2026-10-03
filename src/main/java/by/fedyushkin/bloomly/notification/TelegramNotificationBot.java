package by.fedyushkin.bloomly.notification;

import by.fedyushkin.bloomly.service.TelegramChatBindingService;
import by.fedyushkin.bloomly.util.PhoneNumbers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramNotificationBot extends TelegramLongPollingBot {

    public static final String BOT_HINT =
            "Не удалось отправить код. Откройте Telegram-бота Bloomly, нажмите /start и поделитесь номером телефона.";

    @Value("${telegram.bot.name}")
    private String botUsername;

    private final TelegramChatBindingService telegramChatBindingService;

    public TelegramNotificationBot(
            @Value("${telegram.bot.token}") String botToken,
            TelegramChatBindingService telegramChatBindingService
    ) {
        super(botToken);
        this.telegramChatBindingService = telegramChatBindingService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();

            if (update.getMessage().hasContact()) {
                Contact contact = update.getMessage().getContact();
                String phoneNumber = PhoneNumbers.normalize(contact.getPhoneNumber());
                telegramChatBindingService.bind(phoneNumber, chatId, displayName(contact));
                sendText(chatId, "Спасибо! Ваш номер " + phoneNumber + " успешно привязан. Теперь вы будете получать уведомления.");
                return;
            }

            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                if ("/start".equals(text)) {
                    sendContactRequestKeyboard(chatId);
                }
            }
        }
    }

    public boolean sendMessageByPhone(String phoneNumber, String text) {
        Long chatId = telegramChatBindingService.findChatId(phoneNumber).orElse(null);
        if (chatId == null) {
            return false;
        }
        return sendText(chatId, text);
    }

    private String displayName(Contact contact) {
        String firstName = contact.getFirstName() == null ? "" : contact.getFirstName();
        String lastName = contact.getLastName() == null ? "" : contact.getLastName();
        String name = (firstName + " " + lastName).trim();
        return name.isBlank() ? null : name;
    }

    private void sendContactRequestKeyboard(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Добро пожаловать! Чтобы получать уведомления, пожалуйста, поделитесь своим номером телефона.");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton button = new KeyboardButton("📱 Поделиться номером телефона");
        button.setRequestContact(true);

        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean sendText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }
}
