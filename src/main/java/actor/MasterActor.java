package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import message.AccumulatedResultMessage;
import message.ResponseMessage;
import message.SearchMessage;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MasterActor extends AbstractActor {

    private final Map<String, List<String>> results = new ConcurrentHashMap<>();
    private final AtomicInteger countChildrenWithoutResponse = new AtomicInteger(2);
    private final AtomicBoolean isStopProcessOngoing = new AtomicBoolean(false);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchMessage.class, this::handleSearchMessage)
                .build();
    }

    private void handleSearchMessage(SearchMessage searchMessage) {
        ActorRef main = getContext().getSender();

        Props propsYa = Props.create(YandexSearchActor.class);
        Props propsGo = Props.create(GoogleSearchActor.class);
        askSearch(propsYa, main, searchMessage);
        askSearch(propsGo, main, searchMessage);

    }

    private void askSearch(Props propsYa, ActorRef main, SearchMessage searchMessage) {
        ActorRef actorRef = getContext().actorOf(propsYa);
        CompletableFuture<Object> future = Patterns.ask(actorRef, searchMessage, Duration.ofSeconds(3)).toCompletableFuture();
        future.whenComplete((res, exc) -> {
            if (exc != null && isStopProcessOngoing.compareAndSet(false, true)) {
                main.tell(new AccumulatedResultMessage(results), getSelf());
                getContext().stop(getSelf());
            } else if (exc == null) {
                parseResponse(res, main);
            }
        });

    }

    private void parseResponse(Object result, ActorRef main) {
        if (result instanceof ResponseMessage responseMessage) {
            results.put(responseMessage.searchEngine(), responseMessage.response());
            int currentCount = countChildrenWithoutResponse.decrementAndGet();
            if (currentCount == 0) {
                main.tell(new AccumulatedResultMessage(results), getSelf());
                getContext().stop(getSelf());
            }

        } else {
            throw new IllegalStateException("Unexpected response message of class " + result.getClass().getName());
        }
    }
}
