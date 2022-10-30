package groove.behaviortransformer;

import java.io.IOException;

public class FolderCouldNotBeCleanedException extends RuntimeException {
  public FolderCouldNotBeCleanedException(String message, IOException e) {
    super(message, e);
  }
}
