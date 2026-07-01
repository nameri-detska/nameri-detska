package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal.pdf;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityOwnershipType;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class AIPrivateNurseryPdfParser implements PrivateNurseryPdfParser {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        You are a data extraction tool that parses Bulgarian documents listing private children's nurseries and kindergartens.
        Extract every facility from the text. For each facility:
        - "name": the full name of the facility as written
        - "address": the full address as written, starting from "гр. София" or "с." or "жк."

        Return ONLY a valid JSON array of objects. No markdown, no explanations, no code fences.
        Example: [{"name":"Частна детска ясла \\"Слънце\\"","address":"гр. София, ул. Примерна №1"}]
        """;

    @Override
    public List<KidFacilityDto> parse(byte[] pdfBytes) {
        String text = extractText(pdfBytes);

        String aiResponse = callGemini(text);
        if (aiResponse == null) {
            return List.of();
        }

        List<FacilityRecord> records = parseResponse(aiResponse);
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        return records.stream()
            .filter(r -> r.name() != null && !r.name().isBlank())
            .filter(r -> r.address() != null && !r.address().isBlank())
            .map(r -> KidFacilityDto.builder()
                .name(r.name())
                .address(r.address())
                .kidFacilityType(KidFacilityType.NURSERY)
                .kidFacilityOwnershipType(KidFacilityOwnershipType.PRIVATE_SRZI)
                .build())
            .toList();
    }

    private String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new PdfExtractionException(e);
        }
    }

    private String callGemini(String text) {
        try {
            ChatRequest request = ChatRequest.builder()
                .messages(SystemMessage.from(SYSTEM_PROMPT), UserMessage.from(text))
                .build();

            ChatResponse response = chatModel.chat(request);
            String content = response.aiMessage().text();

            if (content == null || content.isBlank()) {
                log.warn("Gemini returned empty response");
                return null;
            }

            return content;
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return null;
        }
    }

    private List<FacilityRecord> parseResponse(String aiResponse) {
        try {
            String json = extractJson(aiResponse);
            return objectMapper.readValue(
                json, new TypeReference<>() {

                }
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response as JSON. Raw response: {}", aiResponse, e);
            return List.of();
        }
    }

    private String extractJson(String response) {
        String trimmed = response.trim();
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');

        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        log.warn("Gemini response does not contain a JSON array, returning as-is");
        return trimmed;
    }

    private record FacilityRecord(String name, String address) {

    }
}
