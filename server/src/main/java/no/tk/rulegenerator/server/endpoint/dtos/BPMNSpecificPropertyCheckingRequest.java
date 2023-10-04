package no.tk.rulegenerator.server.endpoint.dtos;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record BPMNSpecificPropertyCheckingRequest(
    MultipartFile file, Set<BPMNSpecificProperty> propertiesToBeChecked) {}
