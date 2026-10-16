package com.banco.onboarding.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(Integer id) {
        super("No existe un usuario con el id " + id);
    }
}