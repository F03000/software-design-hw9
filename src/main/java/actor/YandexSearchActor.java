package actor;

import java.net.URI;

public class YandexSearchActor extends AbstractSearchActor {


    public YandexSearchActor() {
        super("Yandex", URI.create("http://localhost:8080/yandex/search"));
    }
}
