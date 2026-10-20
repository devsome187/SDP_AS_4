package proctoring.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonFileStoreTest {

    static class Item {
        private String id;
        private String name;

        private Item() {
        }

        Item(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @TempDir
    Path dir;

    @Test
    void savedItemsAreReadBackByNewStore() {
        new JsonFileStore<>(dir.resolve("items.json"), Item.class, i -> i.id).save(new Item("1", "first"));
        new JsonFileStore<>(dir.resolve("items.json"), Item.class, i -> i.id).save(new Item("1", "changed"));

        JsonFileStore<Item> store = new JsonFileStore<>(dir.resolve("items.json"), Item.class, i -> i.id);
        assertEquals(1, store.findAll().size());
        assertEquals("changed", store.findById("1").orElseThrow().name);
    }

    @Test
    void brokenFileIsReported() throws Exception {
        Files.writeString(dir.resolve("items.json"), "not json");

        JsonFileStore<Item> store = new JsonFileStore<>(dir.resolve("items.json"), Item.class, i -> i.id);
        assertThrows(UncheckedIOException.class, store::findAll);
    }
}
