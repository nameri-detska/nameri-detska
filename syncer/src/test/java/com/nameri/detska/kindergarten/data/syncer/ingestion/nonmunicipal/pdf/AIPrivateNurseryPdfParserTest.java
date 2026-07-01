package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal.pdf;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(AIPrivateNurseryPdfParserTest.TestConfig.class)
class AIPrivateNurseryPdfParserTest {

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "quarkus.datasource.devservices.enabled", "false",
                "private.mon.kindergartens.register.url", "https://example.com/register",
                "private.mon.kindergartens.detail.url", "https://example.com/detail"
            );
        }
    }

    @InjectMock
    ChatModel chatModel;

    @Inject
    AIPrivateNurseryPdfParser parser;

    @Test
    void shouldParseValidRecords() throws Exception {
        mockGeminiResponse("""
            [
              { "name": "Частна детска ясла \\"Слънце\\"", "address": "гр. София, ул. Примерна №1" },
              { "name": "Частна детска ясла \\"Звездичка\\"", "address": "гр. София, ул. Детска №2" }
            ]
            """);

        List<KidFacilityDto> results = parser.parse(createMinimalPdf());

        assertEquals(2, results.size());
        assertEquals("Частна детска ясла \"Слънце\"", results.get(0).name());
        assertEquals("гр. София, ул. Примерна №1", results.get(0).address());
        assertEquals(KidFacilityType.NURSERY, results.get(0).kidFacilityType());
    }

    @ParameterizedTest
    @MethodSource
    void shouldParseAndFilterNurseryRecords(String geminiResponse, int expectedCount, String expectedName) throws Exception {
        mockGeminiResponse(geminiResponse);

        List<KidFacilityDto> results = parser.parse(createMinimalPdf());

        assertEquals(expectedCount, results.size());
        if (expectedCount > 0) {
            assertEquals(expectedName, results.get(0).name());
        }
    }

    static Stream<Arguments> shouldParseAndFilterNurseryRecords() {
        return Stream.of(
            Arguments.of(
                """
                    ```json
                    [
                      { "name": "Тестова ясла", "address": "гр. София, ул. Тест №1" }
                    ]
                    ```
                    """, 1, "Тестова ясла"
            ),
            Arguments.of(
                """
                    [
                      { "name": null, "address": "гр. София, ул. Без име" },
                      { "name": "Валидна ясла", "address": "гр. София, ул. Валидна №1" }
                    ]
                    """, 1, "Валидна ясла"
            ),
            Arguments.of(
                """
                    [
                      { "name": "Ясла без адрес", "address": "" },
                      { "name": "Ясла с адрес", "address": "гр. София, ул. Реална №1" }
                    ]
                    """, 1, "Ясла с адрес"
            )
        );
    }

    @Test
    void shouldReturnEmptyListForNullResponse() throws Exception {
        AiMessage aiMessage = AiMessage.from("");
        ChatResponse empty = ChatResponse.builder().aiMessage(aiMessage).build();
        when(chatModel.chat(any(ChatRequest.class))).thenReturn(empty);

        List<KidFacilityDto> results = parser.parse(createMinimalPdf());
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForInvalidJson() throws Exception {
        mockGeminiResponse("not valid json at all");

        List<KidFacilityDto> results = parser.parse(createMinimalPdf());
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenGeminiFails() throws Exception {
        when(chatModel.chat(any(ChatRequest.class)))
            .thenThrow(new RuntimeException("API error"));

        List<KidFacilityDto> results = parser.parse(createMinimalPdf());
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForEmptyResponse() throws Exception {
        mockGeminiResponse("   ");

        List<KidFacilityDto> results = parser.parse(createMinimalPdf());
        assertTrue(results.isEmpty());
    }

    private void mockGeminiResponse(String content) {
        AiMessage aiMessage = content != null ? AiMessage.from(content) : AiMessage.from("");
        ChatResponse response = ChatResponse.builder().aiMessage(aiMessage).build();
        when(chatModel.chat(any(ChatRequest.class))).thenReturn(response);
    }

    private byte[] createMinimalPdf() throws IOException {
        PDDocument document = new PDDocument();
        document.addPage(new PDPage());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return out.toByteArray();
    }
}
