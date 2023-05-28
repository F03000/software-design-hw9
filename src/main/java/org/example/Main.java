package org.example;

import actor.MasterActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import message.AccumulatedResultMessage;
import message.SearchMessage;
import server.HttpServerStub;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Main {
    private static final int SERVER_LAG_IN_MILLIS = 0;
    private static final String TEST_SEARCH_QUERY = "test_query";

    public static void main(String[] args) {
        try {
            HttpServerStub httpServerStub = new HttpServerStub(SERVER_LAG_IN_MILLIS);
            Thread.sleep(1000); // Delay to start the server

            ActorSystem actorSystem = ActorSystem.create("MySystem");
            Props props = Props.create(MasterActor.class);
            ActorRef actor = actorSystem.actorOf(props, "master");

            Patterns.ask(actor, new SearchMessage(TEST_SEARCH_QUERY), Duration.ofSeconds(10))
                    .toCompletableFuture()
                    .thenAccept(handleAccumulatedResult(httpServerStub));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create stub server:" + e.getMessage());
        }
    }

    private static Consumer<Object> handleAccumulatedResult(HttpServerStub httpServerStub) {
        return result -> {
            if (result instanceof AccumulatedResultMessage accumulatedResultMessage) {
                System.out.println("Received result of size:" + accumulatedResultMessage.result().size());
                for (Map.Entry<String, List<String>> entry : accumulatedResultMessage.result().entrySet()) {
                    System.out.println("Search engine: " + entry.getKey());
                    for (String curStr : entry.getValue()) {
                        System.out.println(curStr);
                    }
                }
                try {
                    httpServerStub.stop();
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to stop stub server:" + e.getMessage());
                }
            }
        };
    }
}