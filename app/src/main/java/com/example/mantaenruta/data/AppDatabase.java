package com.example.mantaenruta.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Usuario.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "login_usuarios.db"
                    ).build();
                }
            }
        }
        return instance;
    }
}
