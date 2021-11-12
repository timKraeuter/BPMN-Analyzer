package behavior.piCalculus;

/**
 * Singular sum is a prefixed process: a.P
 */
public class PrefixedProcess extends Sum {
    private final Prefix prefix;
    private final PiProcess process;

    public PrefixedProcess(Prefix prefix, PiProcess process) {
        this.prefix = prefix;
        this.process = process;
    }
}
