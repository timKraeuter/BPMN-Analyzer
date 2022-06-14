package behavior.picalculus;

import java.util.Set;

public class Prefix {
    private final PrefixType prefixType;
    private final String channel;
    private final Set<String> parameters;

    public Prefix(PrefixType prefixType, String channel, Set<String> parameters) {
        this.prefixType = prefixType;
        this.channel = channel;
        this.parameters = parameters;
    }

    public PrefixType getPrefixType() {
        return this.prefixType;
    }

    public String getChannel() {
        return this.channel;
    }

    public Set<String> getPayloads() {
        return this.parameters;
    }
}
