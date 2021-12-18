package wtf.zv.wowzers;

import static reactor.bool.BooleanUtils.*;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.UserData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wtf.zv.wowzers.internal.ResponseData;

import java.util.Set;

public class Client {
    private static final Snowflake GUILD_ID = Snowflake.of("705157826204401665");
    private static final Set<Snowflake> CHANNEL_IDS = Set.of(
            Snowflake.of("908887065406603284"), Snowflake.of("920086910288949268"));
    private static final Id USER_OLIVER_ID = Id.of("106075371031511040");

    private static final Set<String> TRIGGERS = Set.of("?", "*", "upvote", "vote");

    private static final Set<String> VOTE_EMOJIS = Set.of(/* upvote = */ "U+2B07", /* downvote = */ "U+2B06");
    private static final Snowflake OLIVER_EMOJI_ID = Snowflake.of("856938969136496670");
    private static final String FACE_PALM_EMOJI = "U+1F926";

    public static void main(String[] args) {
        final DiscordClient client = DiscordClient.create("OTIwMDY3MjQyNjU2NDExNjg5.Ybe9ZA.PlJCe1ns76bfHehaO8VJJrqCdR4");

        client.login()
                .flatMapMany(gateway -> gateway.on(MessageCreateEvent.class))
                .filter(createEvent -> createEvent.getGuildId().isPresent()
                        && createEvent.getGuildId().get().equals(GUILD_ID))
                .map(MessageCreateEvent::getMessage)
                .map(Client::getResponseData)
                .filterWhen(ResponseData::shouldRespond)
                .doOnNext(Client::addReactions)
                .blockLast();
    }

    private static void addReactions(ResponseData responseData){
        Message message = responseData.getMessage();
        System.out.println("Adding reaction to: " + message.getContent());

        Mono<Void> specialPub = Mono.just(responseData)
                .filterWhen(ResponseData::isOliver)
                .flatMap(k -> message.addReaction(ReactionEmoji.custom(OLIVER_EMOJI_ID, "", false)))
                .flatMap(k -> message.addReaction(ReactionEmoji.codepoints(FACE_PALM_EMOJI)));

        Flux<Void> commonPub = Flux.fromIterable(VOTE_EMOJIS)
                .flatMap(codepoint -> message.addReaction(ReactionEmoji.codepoints(codepoint)));

        specialPub.subscribe();
        commonPub.subscribe();
    }

    private static ResponseData getResponseData(Message message){
        /*
         * These Monos are currently being cached so that they can be subscribed to later on from the addReactions
         * method, if they were not cached this would cause a "stream has already been operated upon or closed" error,
         * as after a Mono has been subscribed to, it can not be subscribed to again. The cache method transforms a
         * given Mono into a hot source, meaning it will replay it's previous value.
         *
         * Each Mono should be refactored into a Publisher<Boolean> which can serve subscribers an unbounded number of
         * times.
         */

        Mono<Boolean> isCorrectChannel = message.getChannel().map(Entity::getId).map(CHANNEL_IDS::contains).cache();
        Mono<Boolean> isOliver = Mono.just(message)
                .flatMap(Message::getAuthorAsMember)
                .map(Member::getMemberData)
                .map(MemberData::user)
                .map(UserData::id)
                .map(USER_OLIVER_ID::equals)
                .cache();
        Mono<Boolean> isTriggerPhrase = Flux.fromStream(TRIGGERS.stream())
                .reduce(false, (k, t) -> k || message.getContent().toLowerCase().startsWith(t))
                .map(k -> k || message.getContent().trim().endsWith("?"))
                .cache();
        Mono<Boolean> hasAttachments = Mono.just(!message.getAttachments().isEmpty()).cache();

        return ResponseData.of(
                message,
                and(isCorrectChannel, or(isTriggerPhrase, hasAttachments)),
                isOliver
        );
    }
}