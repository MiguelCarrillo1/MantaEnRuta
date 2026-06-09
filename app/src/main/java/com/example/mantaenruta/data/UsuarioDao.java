package com.example.mantaenruta.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertar(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE correo = :login OR usuario = :login LIMIT 1")
    Usuario buscarPorLogin(String login);

    @Query("SELECT * FROM usuarios WHERE correo = :correo OR usuario = :usuario LIMIT 1")
    Usuario buscarDuplicado(String correo, String usuario);

    @Query("SELECT COUNT(*) FROM usuarios")
    int contarUsuarios();

    @Query("SELECT * FROM usuarios ORDER BY id DESC")
    Flow<List<Usuario>> observarUsuarios();
}
