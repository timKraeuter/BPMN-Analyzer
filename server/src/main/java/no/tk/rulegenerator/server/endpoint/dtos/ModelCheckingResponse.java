package no.tk.rulegenerator.server.endpoint.dtos;

public record ModelCheckingResponse(String property, boolean valid, String error) {}
