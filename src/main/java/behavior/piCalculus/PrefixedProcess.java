package behavior.piCalculus;

/**
 * Singular sum is a prefixed process: a!(x).P/a?(y).P .
 */
public class PrefixedProcess extends Sum {
    private final Prefix prefix;
    private final PiProcess process;

    public PrefixedProcess(Prefix prefix, PiProcess process) {
        this.prefix = prefix;
        this.process = process;
    }

    @Override
    public void accept(PiProcessVisitor visitor) {
        visitor.handle(this);
    }
}
