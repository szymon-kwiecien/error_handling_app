package pl.error_handling_app.mail;

public record EmailData(String to, String subject, String body, String category) {}