package no.tk.rulegenerator.server.endpoint.dtos;

import no.tk.groove.runner.checking.TemporalLogic;
import org.springframework.web.multipart.MultipartFile;

public record ModelCheckingRequest(
    MultipartFile file, String property, TemporalLogic logic, String propositions) {}
