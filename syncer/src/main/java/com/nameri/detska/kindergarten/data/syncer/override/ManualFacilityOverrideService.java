package com.nameri.detska.kindergarten.data.syncer.override;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityRepository;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ManualFacilityOverrideService {

    private static final String OVERRIDES_FILEPATH = "overrides.txt";

    private final KidFacilityRepository repository;

    private final Map<String, double[]> coordinatesOverrides = new LinkedHashMap<>();
    private final Map<String, String> addressesOverrides = new LinkedHashMap<>();

    @PostConstruct
    void loadOverrides() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(OVERRIDES_FILEPATH)) {
            if (is == null) {
                log.info("No overrides file '{}' found on classpath, skipping", OVERRIDES_FILEPATH);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\|", 3);
                if (parts.length != 3) {
                    log.warn("Skipping malformed override line: {}", line);
                    continue;
                }

                String type = parts[0].strip();
                String key = parts[1].strip();
                String value = parts[2].strip();

                switch (type) {
                    case "coordinates":
                        String[] coords = value.split(",");
                        coordinatesOverrides.put(
                            key, new double[]{
                                Double.parseDouble(coords[0].trim()),
                                Double.parseDouble(coords[1].trim())
                            }
                        );
                        break;
                    case "address":
                        addressesOverrides.put(key, value);
                        break;
                    default:
                        log.warn("Unknown override type '{}' in line: {}", type, line);
                }
            }

            log.info(
                "Loaded {} coordinates and {} address overrides from '{}'",
                coordinatesOverrides.size(), addressesOverrides.size(), OVERRIDES_FILEPATH
            );
        } catch (IOException e) {
            log.error("Failed to read overrides file '{}'", OVERRIDES_FILEPATH, e);
        }
    }

    @Transactional
    public void applyOverrides() {
        for (var entry : coordinatesOverrides.entrySet()) {
            repository.find("address", entry.getKey())
                .firstResultOptional()
                .ifPresent(facility -> {
                    double[] coords = entry.getValue();
                    log.info("Overriding coordinates for '{}': {}, {}", entry.getKey(), coords[0], coords[1]);
                    facility.latitude(coords[0]);
                    facility.longitude(coords[1]);
                });
        }

        for (var entry : addressesOverrides.entrySet()) {
            repository.find("address", entry.getKey())
                .firstResultOptional()
                .ifPresent(facility -> {
                    log.info("Overriding address for '{}': {}", entry.getKey(), entry.getValue());
                    facility.address(entry.getValue());
                });
        }
    }
}
