package behavior.picalculus;

import java.util.Objects;

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

    @Override
    public boolean isEmptySum() {
        return false;
    }

    public String getRestrictedName() {
        return restrictedName;
    }

    public PiProcess getRestrictedProcess() {
        return restrictedProcess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameRestriction that = (NameRestriction) o;
        return Objects.equals(restrictedName, that.restrictedName) && Objects.equals(restrictedProcess, that.restrictedProcess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restrictedName, restrictedProcess);
    }
}
