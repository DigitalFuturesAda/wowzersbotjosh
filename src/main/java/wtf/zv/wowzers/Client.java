package wtf.zv.wowzers;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static final Snowflake GUILD_ID = Snowflake.of("705157826204401665");
    private static final String[] TRIGGERS = {"?", "*", "upvote", "vote", "redditmoment"};

    private static final String UPVOTE_EMOJI = "U+2B06";
    private static final String DOWN_VOTE_EMOJI = "U+2B07";

    public static void main(String[] args) {
        final DiscordClient client = DiscordClient.create("TOKEN!");

        client.login()
                .flatMapMany(gateway -> gateway.on(MessageCreateEvent.class))
                .filter(createEvent -> createEvent.getGuildId().isPresent()
                        && createEvent.getGuildId().get().equals(GUILD_ID))
                .map(MessageCreateEvent::getMessage)
                .filter(Client::isTriggerPhrase)
                .doOnNext(Client::addReactions)
                .blockLast();
    }

    private static void addReactions(Message message){
        System.out.println("Adding reaction to: " + message.getContent());
        message.addReaction(ReactionEmoji.codepoints(UPVOTE_EMOJI)).subscribe();
        message.addReaction(ReactionEmoji.codepoints(DOWN_VOTE_EMOJI)).subscribe();
    }

    private static boolean isTriggerPhrase(Message message){
        AtomicBoolean flag = new AtomicBoolean(false);

        Arrays.asList(TRIGGERS).forEach(trigger -> {
            if (message.getContent().toLowerCase().startsWith(trigger)
                    && !message.getContent().toLowerCase().equals(trigger)) flag.set(true);
            if (message.getContent().toLowerCase().endsWith("?") && !message.getContent().equalsIgnoreCase("?")) flag.set(true);
        });

        if (!message.getAttachments().isEmpty()) flag.set(true);
        return flag.get();
    }
}