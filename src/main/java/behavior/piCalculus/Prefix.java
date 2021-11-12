package behavior.piCalculus;

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
}
