package no.tk.behavior.picalculus;

public interface PiProcess {
  <T> T accept(PiProcessVisitor<T> visitor);

  boolean isEmptySum();
}
