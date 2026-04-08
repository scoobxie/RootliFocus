package com.onrender.rootlify;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class TwitchBot {

    public static void startBot(String channelName) {
        if (channelName == null || channelName.isEmpty()) return;

        TwitchClient twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .build();

        twitchClient.getChat().joinChannel(channelName);

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            String user = event.getUser().getName();
            String message = event.getMessage();

            // Command: !task
            if (message.toLowerCase().startsWith("!task ")) {
                String taskName = message.substring(6).trim();
                if (!taskName.isEmpty()) {
                    MainApp.addViewerTask(user, taskName);
                }
            }

            // Command: !done
            if (message.equalsIgnoreCase("!done")) {
                MainApp.completeViewerTask(user);
            }
        });
    }
}