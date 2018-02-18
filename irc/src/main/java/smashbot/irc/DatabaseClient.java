package smashbot.irc;

import redis.clients.jedis.Jedis;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class DatabaseClient {

    private static final String FIELD_SEPARATOR = ".";

    private Jedis jedis;

    public DatabaseClient(String hostname) {
        jedis = new Jedis(hostname);
    }

    private void set(String key, Number value) {
        jedis.set(key, value.toString());
    }

    private void set(String key, String value) {
        jedis.set(key, value);
    }

    private boolean exists(String key) {
        return jedis.exists(key);
    }

    private String get(String key) {
        return jedis.get(key);
    }

    private void delete(String... keys) {
        jedis.del(keys);
    }

    private String createKey(String... components) {
        checkNotNull(components);
        checkArgument(components.length > 0);
        Arrays.asList(components).forEach(c -> checkArgument(!c.isEmpty()));
        return String.join(FIELD_SEPARATOR, components);
    }

    public void createUser(String username) {
        checkArgument(!username.isEmpty());
        String lookupKey = createKey(username, "elo");
        checkState(!exists(lookupKey));
        set(lookupKey, Constants.START_ELO);
    }
}
