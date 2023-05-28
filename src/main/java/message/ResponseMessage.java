package message;

import java.util.List;

public record ResponseMessage(String searchEngine, List<String> response) {
}
