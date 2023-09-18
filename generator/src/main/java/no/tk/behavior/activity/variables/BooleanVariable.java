package no.tk.behavior.activity.variables;

import no.tk.behavior.activity.values.BooleanValue;

public class BooleanVariable extends Variable<BooleanValue> {

  public BooleanVariable(String name, boolean initialValue) {
    super(name, new BooleanValue(initialValue));
  }
}
