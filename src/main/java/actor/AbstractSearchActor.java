package actor;

import akka.actor.AbstractActor;
import message.ResponseMessage;
import message.SearchMessage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractSearchActor extends AbstractActor {
    private final String searchEngineName;
    private final URI searchUri;
    private static final int RETURN_TOP_N_RESULTS = 5;

    protected AbstractSearchActor(String searchEngineName, URI searchUri) {
        this.searchEngineName = searchEngineName;
        this.searchUri = searchUri;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchMessage.class, this::apply)
                .build();
    }

    private void apply(SearchMessage a) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(searchUri)
                .header("accept", "application/json")
                .header("query", a.query())
                .build();

        HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonObject = new JSONObject(send.body());
        List<String> resultList = jsonObject.getJSONArray(a.query()).toList().stream().map(Object::toString).collect(Collectors.toList());
        getContext().getSender().tell(new ResponseMessage(searchEngineName, resultList.subList(0, RETURN_TOP_N_RESULTS)), getSelf());
    }
}
