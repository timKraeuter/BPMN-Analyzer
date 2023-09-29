package no.tk.rulegenerator.server.endpoint.dtos;

import java.util.HashSet;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public class BPMNSpecificPropertyCheckingRequest {
  // TODO: This should be able to be a record class.
  MultipartFile file;
  Set<BPMNSpecificProperty> propertiesToBeChecked;

  public BPMNSpecificPropertyCheckingRequest() {
    propertiesToBeChecked = new HashSet<>();
  }

  public MultipartFile getFile() {
    return file;
  }

  public Set<BPMNSpecificProperty> getPropertiesToBeChecked() {
    return propertiesToBeChecked;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }

  public void setPropertiesToBeChecked(Set<BPMNSpecificProperty> propertiesToBeChecked) {
    this.propertiesToBeChecked = propertiesToBeChecked;
  }
}
