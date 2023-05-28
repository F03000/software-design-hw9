package message;

import java.util.List;
import java.util.Map;

public record AccumulatedResultMessage(Map<String, List<String>> result) {
}


