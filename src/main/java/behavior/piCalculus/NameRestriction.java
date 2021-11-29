package behavior.piCalculus;

public class NameRestriction implements PiProcess {
    private final String restrictedName;
    private final PiProcess restrictedProcess;

    public NameRestriction(String restrictedName, PiProcess restrictedProcess) {
        this.restrictedName = restrictedName;
        this.restrictedProcess = restrictedProcess;
    }

    @Override
    public <T> T accept(PiProcessVisitor<T> visitor) {
        return visitor.handle(this);
    }
}
