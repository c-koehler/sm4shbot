package smashbot.irc;

public class BotConfig {

    private String name;
    private String server;
    private String channel;

    public BotConfig(String name, String server, String channel) {
        this.name = name;
        this.server = server;
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    public String getChannel() {
        return channel;
    }
}
