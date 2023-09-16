package com.apnaclassroom.exception;

public class InsufficientRolesException extends RuntimeException{
    public InsufficientRolesException(String message) {
        super(message);
    }
}
