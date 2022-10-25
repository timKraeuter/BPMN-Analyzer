package behavior.picalculus;

import java.util.Objects;
import java.util.Set;

/**
 * Singular sum is a prefixed process: a!(x).P/a?(y).P .
 */
public class PrefixedProcess implements Sum {
    private final Prefix prefix;
    private final PiProcess process;

    public PrefixedProcess(Prefix prefix, PiProcess process) {
        this.prefix = prefix;
        this.process = process;
    }

    @Override
    public <T> T accept(PiProcessVisitor<T> visitor) {
        return visitor.handle(this);
    }

    @Override
    public boolean isEmptySum() {
        return false;
    }

    public PrefixType getPrefixType() {
        return this.prefix.getPrefixType();
    }

    public PiProcess getProcess() {
        return this.process;
    }

    public String getChannel() {
        return this.prefix.getChannel();
    }

    public String getPayload() {
        Set<String> payloads = this.prefix.getPayloads();
        // For now we only allow the pi-calculus to send and receive one name at a time.
        assert payloads.size() == 1;
        return payloads.iterator().next();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrefixedProcess)) return false;
        PrefixedProcess that = (PrefixedProcess) o;
        return Objects.equals(prefix, that.prefix) && Objects.equals(process, that.process);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, process);
    }
}
