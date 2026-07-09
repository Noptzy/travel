package com.makeyourjurney.application.auth;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Email sudah terdaftar");
    }
}
