package ru.mtuci.rbpo.lab2.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonHousingRepository {
    private final ObjectMapper mapper;
    private final Path file;
    private HousingData data;

    public JsonHousingRepository(ObjectMapper mapper, @Value("${housing.storage-file}") String filename)
            throws IOException {
        this.mapper = mapper;
        this.file = Path.of(filename);
        Files.createDirectories(file.toAbsolutePath().getParent());

        if (Files.exists(file)) {
            data = mapper.readValue(file.toFile(), HousingData.class);
        } else {
            data = HousingData.empty();
        }
    }

    public HousingData getData() {
        return data;
    }

    public void save(HousingData updated) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), updated);
            data = updated;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
