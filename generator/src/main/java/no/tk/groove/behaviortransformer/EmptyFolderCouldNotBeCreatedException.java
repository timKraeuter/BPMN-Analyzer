package no.tk.groove.behaviortransformer;

import java.io.IOException;

public class EmptyFolderCouldNotBeCreatedException extends RuntimeException {
  public EmptyFolderCouldNotBeCreatedException(String message, IOException e) {
    super(message, e);
  }
}
