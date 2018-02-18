package smashbot.irc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Bot extends PircBotX {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private static final String CREATE_CONFIG_COMMAND = "--create-config";
    private static final String CONFIG_PATH = "config.json";
    private static final String DEFAULT_USERNAME = "sm4shbot";
    private static final String DEFAULT_SERVER = "irc.freenode.net";
    private static final String DEFAULT_CHANNEL = "#sm4shchannel";

    private DatabaseClient databaseClient;

    public Bot(Configuration configuration, DatabaseClient databaseClient) {
        super(configuration);
        this.databaseClient = databaseClient;
    }

    private static void createConfig() throws Exception {
        if (Files.exists(Paths.get(CONFIG_PATH))) {
            log.error("Cannot create a new config, one already exists!");
            throw new Exception();
        }

        BotConfig botConfig = new BotConfig(DEFAULT_USERNAME, DEFAULT_SERVER, DEFAULT_CHANNEL);
        try (Writer writer = new FileWriter(CONFIG_PATH)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(botConfig, writer);
            log.info("Created config file {}", CONFIG_PATH);
        }
    }

    private static BotConfig readBotConfig() throws FileNotFoundException {
        try {
            JsonReader reader = new JsonReader(new FileReader(CONFIG_PATH));
            return new Gson().fromJson(reader, BotConfig.class);
        } catch (FileNotFoundException e) {
            log.error("Unable to find config file at: {}", CONFIG_PATH);
            log.error("To create a template config, run this with: {}", CREATE_CONFIG_COMMAND);
            throw e;
        }
    }

    private static Configuration createConfiguration(BotConfig botConfig) {
        return new Configuration.Builder()
                .setName(botConfig.getName())
                .addServer(botConfig.getServer())
                .addAutoJoinChannel(botConfig.getChannel())
                .addListener(new IrcEventListener())
                .buildConfiguration();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase(CREATE_CONFIG_COMMAND)) {
            createConfig();
            return;
        }

        BotConfig botConfig = readBotConfig();
        Configuration ircConfig = createConfiguration(botConfig);
        Bot bot = new Bot(ircConfig, new DatabaseClient());
        log.info("Connecting to server {} with name {} on channel {}.", botConfig.getServer(), botConfig.getName(), botConfig.getChannel());
        bot.startBot();
    }
}
