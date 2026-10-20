package proctoring.persistence;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class JsonFileStore<T> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private final Path file;
    private final CollectionType listType;
    private final Function<T, String> idOf;

    public JsonFileStore(Path file, Class<T> type, Function<T, String> idOf) {
        this.file = file;
        this.listType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
        this.idOf = idOf;
    }

    public List<T> findAll() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(file.toFile(), listType);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    public Optional<T> findById(String id) {
        return findAll().stream().filter(item -> idOf.apply(item).equals(id)).findFirst();
    }

    public void save(T item) {
        List<T> items = findAll();
        items.removeIf(existing -> idOf.apply(existing).equals(idOf.apply(item)));
        items.add(item);
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            MAPPER.writeValue(file.toFile(), items);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }
}
