import actor.MasterActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import message.AccumulatedResultMessage;
import message.SearchMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.HttpServerStub;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class NoTimeoutSearchTest {
    public static HttpServerStub httpServerStub;

    @BeforeAll
    public static void init() {
        try {
            httpServerStub = new HttpServerStub(0);
            Thread.sleep(1000); // Delay to start the server
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create stub server:" + e.getMessage());
        }
    }

    @AfterAll
    public static void stop() {
        httpServerStub.stop();
    }

    @Test
    public void noTimeoutTest() {
        String query = "test";
        ActorSystem actorSystem = ActorSystem.create("TestSystem");
        Props props = Props.create(MasterActor.class);
        ActorRef actor = actorSystem.actorOf(props, "master");

        Patterns.ask(actor, new SearchMessage(query), Duration.ofSeconds(10))
                .toCompletableFuture()
                .thenAccept(handleAccumulatedResult());
    }

    private Consumer<Object> handleAccumulatedResult() {
        return result -> {
            assertInstanceOf(AccumulatedResultMessage.class, result);
            AccumulatedResultMessage accumulatedResultMessage = (AccumulatedResultMessage) result;

            assertNotNull(accumulatedResultMessage.result());

            assertEquals(Set.of("Google", "Yandex"), accumulatedResultMessage.result().keySet());

            for (Map.Entry<String, List<String>> entry : accumulatedResultMessage.result().entrySet()) {
                assertEquals(5, entry.getValue().size());
            }
        };
    }
}
