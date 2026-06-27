package database;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import repository.NoteRepository;

public class DatabaseInitializer {

    public static void initialize() {

        try {

            File dataFolder = new File("data");

            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            try (Connection con = Database.getConnection();
                 Statement stmt = con.createStatement()) {

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS note(
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            parent_id INTEGER,
                            title TEXT NOT NULL,
                            memo TEXT,
                            sort_order INTEGER NOT NULL DEFAULT 0,
                            created_at TEXT,
                            updated_at TEXT
                        );
                        """);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        NoteRepository repository = new NoteRepository();

        if (repository.findRoot() == null) {

            repository.insert(
                    null,
                    "上達ノート"
            );

        }

    }

}