package behavior.petrinet;

public class Place {
  private final String name;
  private final Integer startTokenAmount;

  public Place(String name, Integer startTokenAmount) {
    this.name = name;
    this.startTokenAmount = startTokenAmount;
  }

  public Place(String name) {
    this(name, 0);
  }

  public String getName() {
    return this.name;
  }

  public Integer getStartTokenAmount() {
    return this.startTokenAmount;
  }
}
