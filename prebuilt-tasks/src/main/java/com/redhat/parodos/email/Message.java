package com.redhat.parodos.email;

public record Message(String to, String from, String subject, String data) {
}
