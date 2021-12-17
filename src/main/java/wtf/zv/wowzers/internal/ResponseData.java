package wtf.zv.wowzers.internal;

import discord4j.core.object.entity.Message;
import reactor.bool.BooleanUtils;
import reactor.core.publisher.Mono;

public class ResponseData {
    private final Message message;
    private final Mono<Boolean> isCorrectChannel;
    private final Mono<Boolean> isOliver;

    public static ResponseData of(Message message, Mono<Boolean> isCorrectChannel, Mono<Boolean> isOliver){
        return new ResponseData(message, isCorrectChannel, isOliver);
    }

    public Message getMessage() {
        return message;
    }

    public Mono<Boolean> matchesSpec() {
        return isCorrectChannel;
    }

    public Mono<Boolean> shouldRespond() {
        return BooleanUtils.or(isCorrectChannel, isOliver);
    }

    public Mono<Boolean> isOliver() {
        return isOliver;
    }

    private ResponseData(Message message, Mono<Boolean> isCorrectChannel, Mono<Boolean> isOliver) {
        this.message = message;
        this.isCorrectChannel = isCorrectChannel;
        this.isOliver = isOliver;
    }
}