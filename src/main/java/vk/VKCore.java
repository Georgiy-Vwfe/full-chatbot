package vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;


import java.util.List;

public class VKCore {
    //Ключ доступа
    private static final String ACCESS_TOKEN =
            "5ac8c045a4e55ea97f93600ce34cb0908e92a99a1731692f7525eec22e5d51e2a62f5090ed378d112cc68";
    //Id группы
    private static final int GROUP_ID = 201383444;

    public VkApiClient vk;
    public GroupActor actor;
    private static int ts; //Временная метка
    private static int maxMsgId = -1;


    public VKCore() throws ClientException, ApiException {
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        actor = new GroupActor(GROUP_ID, ACCESS_TOKEN);
        ts = getTs();
    }

    public Message getMessage() throws ClientException, ApiException {
        MessagesGetLongPollHistoryQuery eventsQuery =
                vk.messages()
                        .getLongPollHistory(actor)
                        .ts(ts);

        if (maxMsgId > 0) {
            eventsQuery.maxMsgId(maxMsgId);
        }

        List<Message> messages = eventsQuery.execute()
                .getMessages()
                .getItems();

        if (!messages.isEmpty()) {
            try {
                ts = getTs();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }

        // isOut озвращает true если сообщение от чат бота
        if (!messages.isEmpty() && !messages.get(0).isOut()) {
            int messageId = messages.get(0).getId();
            if (messageId > maxMsgId) {
                maxMsgId = messageId;
            }
            return messages.get(0);
        }
        return null;
    }

    private int getTs() throws ClientException, ApiException {
        return vk.messages()
                .getLongPollServer(actor) //задает запрос для метода Messages.getLongPollServer
                .execute() // возвращает LongPollParams
                .getTs(); // возвращает номер последнего события, начиная с которого нужно получить данные
    }
}