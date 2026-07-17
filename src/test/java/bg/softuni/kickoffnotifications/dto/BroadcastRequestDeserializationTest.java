package bg.softuni.kickoffnotifications.dto;

import bg.softuni.kickoffnotifications.model.enums.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BroadcastRequestDeserializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"MATCH_KICKOFF", "MATCH_HALFTIME", "MATCH_FULLTIME", "MATCH_RESULT"})
    void deserializesMatchEventTypes(String type) throws Exception {
        String json = """
                {
                  "matchId": "11111111-1111-1111-1111-111111111111",
                  "homeTeamId": "22222222-2222-2222-2222-222222222222",
                  "awayTeamId": "33333333-3333-3333-3333-333333333333",
                  "message": "test",
                  "type": "%s"
                }
                """.formatted(type);

        BroadcastRequest request = objectMapper.readValue(json, BroadcastRequest.class);

        assertThat(request.getType()).isEqualTo(NotificationType.valueOf(type));
    }

    @Test
    void unknownTypeDeserializesToNullInsteadOfFailing() throws Exception {
        String json = """
                {
                  "matchId": "11111111-1111-1111-1111-111111111111",
                  "homeTeamId": "22222222-2222-2222-2222-222222222222",
                  "awayTeamId": "33333333-3333-3333-3333-333333333333",
                  "message": "test",
                  "type": "SOMETHING_UNKNOWN"
                }
                """;

        BroadcastRequest request = objectMapper.readValue(json, BroadcastRequest.class);

        assertThat(request.getType()).isNull();
    }
}
