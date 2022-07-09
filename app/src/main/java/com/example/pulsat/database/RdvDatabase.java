package com.example.pulsat.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.pulsat.model.Rdv;
import com.example.pulsat.database.dao.RdvDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Rdv.class}, version = 2, exportSchema = false)
public abstract class RdvDatabase extends RoomDatabase {

    // --- SINGLETON ---
    private static volatile RdvDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // --- DAO ---
    public abstract RdvDao rdvDao();
    // --- INSTANCE ---
    public static RdvDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RdvDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RdvDatabase.class, "MyDatabase.db")
                            .addCallback(sRoomDatabaseCallback)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                RdvDao dao = INSTANCE.rdvDao();
            });
        }
    };

}
