package org.oosd.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/*
reads and writes one value of type T to one JSON file
generic so the same code stores the config (a record) and the high
score table (a List<HighScore>) without any casting
*/
public final class JsonStore<T> {

    //ObjectMapper is thread safe once configured, so one shared instance is enough
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            //an older or newer build may have written extra fields, ignore them
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Path file;
    private final JavaType type;

    private JsonStore(Path file, JavaType type) {
        this.file = Objects.requireNonNull(file, "file");
        this.type = type;
    }

    //for plain types such as GameConfig
    public static <T> JsonStore<T> of(Path file, Class<T> type) {
        return new JsonStore<>(file, MAPPER.getTypeFactory().constructType(type));
    }

    //for generic types such as List<HighScore>, where a Class object would lose the element type
    public static <T> JsonStore<T> of(Path file, TypeReference<T> type) {
        return new JsonStore<>(file, MAPPER.getTypeFactory().constructType(type));
    }

    public Path file() {
        return file;
    }

    public boolean exists() {
        return Files.exists(file);
    }

    //empty when the file is missing, unreadable or holds invalid data
    public Optional<T> load() {
        if (!exists()) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T value = MAPPER.readValue(reader, type);
            return Optional.ofNullable(value);
        } catch (IOException | RuntimeException e) {
            //a corrupt or hand-edited file should never stop the game starting
            System.err.println("Could not read " + file.toAbsolutePath() + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public T loadOrElse(Supplier<? extends T> fallback) {
        return load().orElseGet(fallback);
    }

    /*
    writes to a temporary file first and then swaps it into place, so a
    crash halfway through saving can't leave a half written file behind
    */
    public boolean save(T value) {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                MAPPER.writeValue(writer, value);
            }
            moveIntoPlace(temp);
            return true;
        } catch (IOException e) {
            System.err.println("Could not save " + file.toAbsolutePath() + ": " + e.getMessage());
            return false;
        }
    }

    private void moveIntoPlace(Path temp) throws IOException {
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            //some file systems can't do atomic moves, a plain replace is still fine
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
