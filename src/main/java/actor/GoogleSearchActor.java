package actor;

import java.net.URI;

public class GoogleSearchActor extends AbstractSearchActor {
    public GoogleSearchActor() {
        super("Google", URI.create("http://localhost:8080/google/search"));

    }
}
