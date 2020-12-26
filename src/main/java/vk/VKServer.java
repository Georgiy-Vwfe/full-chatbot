package vk;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import model.User;
import model.Word;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class VKServer {
    public static VKCore vkCore;

    public static List<Word> allWord = Arrays.asList(
            new Word("позволять", "allow"),
            new Word("яблоко", "apple"),
            new Word("мясо", "meat"),
            new Word("машина", "car"),
            new Word("дом", "house")
    );
    public static HashMap<Integer, User> userData = new HashMap<>();

    static {
        try {
            vkCore = new VKCore();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running Server...");

        while (true) {
            Thread.sleep(300);
            try {
                Message msg = vkCore.getMessage();
                if (msg != null && (!msg.getText().isEmpty())) {
                    Executors.newCachedThreadPool().execute(() -> sendMessage(getReplyMessage(msg), msg.getPeerId(), msg.getRandomId()));
                }
            } catch (ClientException e) {
                System.out.println("Поторное соединение..");
                Thread.sleep(10000);
            }
        }
    }

    public static void sendMessage(String replyMessage, int userId, int randomId) {
        try {
            vkCore.vk.messages()
                    .send(vkCore.actor)
                    .userId(userId)
                    .randomId(randomId)
                    .message(replyMessage)
                    .execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }

    public static String getReplyMessage(Message msg) {
        String userMessage = msg.getText();
        String replyMessage;
        int userId = msg.getPeerId();


        User data = userData.getOrDefault(userId, null);
        if (data == null) {
            data = new User(userId);
            userData.put(userId, data);
        }

        if (data.isTranslating()) {
            if (data.currentWord.rus.equalsIgnoreCase(userMessage)) {
                data.totalCorrectAnswers++;
                replyMessage = "Верно. \n";
            } else {
                data.totalWrongAnswers++;
                replyMessage = "Ты ошибся. \n";
            }
            replyMessage += "Слово: " + data.currentWord.eng +
                    ", первод: " + data.currentWord.rus;
            data.answeredWords.add(data.currentWord);
            data.currentWord = null;
        } else {
            switch (userMessage.toLowerCase()) {
                case "start":
                    Word translateWord = null;
                    for (Word word : allWord) {
                        if (!data.answeredWords.contains(word)) {
                            translateWord = word;
                            break;
                        }
                    }
                    if (translateWord == null) {
                        replyMessage = "Поздравляю, ты перевел все слова.";
                    } else {
                        data.currentWord = translateWord;
                        replyMessage = "В ответе напиши перевод слова: " + translateWord.eng;
                    }
                    break;
                case "score":
                    replyMessage = "Правильных ответов: " + data.totalCorrectAnswers
                            + "\n Неправильных ответов: " + data.totalWrongAnswers;
                    break;
                default:
                    replyMessage = "Неопознанная команда: '" + userMessage + "'";
                    break;
            }
        }
        return replyMessage;
    }
}