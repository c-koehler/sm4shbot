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

public class IrcBot extends PircBotX {

    private static final Logger log = LoggerFactory.getLogger(IrcBot.class);
    private static final String CREATE_CONFIG_COMMAND = "--create-config";
    private static final String UPDATE_CONFIG_COMMAND = "--update-config";
    private static final String CONFIG_PATH = "config.json";

    public IrcBot(Configuration config) {
        super(config);
    }

    private static void createConfig() throws Exception {
        if (Files.exists(Paths.get(CONFIG_PATH))) {
            log.error("Cannot create a new config, one already exists!");
            throw new Exception();
        }

        try (Writer writer = new FileWriter(CONFIG_PATH)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(new SmashbotConfig(), writer);
            log.info("Created config file: {}", CONFIG_PATH);
        }
    }

    private static void updateConfig() throws Exception {
        if (!Files.exists(Paths.get(CONFIG_PATH))) {
            log.error("Config does not exist, creating new one!");
            createConfig();
            return;
        }

        try (Writer writer = new FileWriter(CONFIG_PATH)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(readSmashbotConfig(), writer);
            log.info("Updated config file: {}", CONFIG_PATH);
        }
    }

    private static SmashbotConfig readSmashbotConfig() throws FileNotFoundException {
        try {
            JsonReader reader = new JsonReader(new FileReader(CONFIG_PATH));
            return new Gson().fromJson(reader, SmashbotConfig.class);
        } catch (FileNotFoundException e) {
            log.error("Unable to find config file at: {}", CONFIG_PATH);
            log.error("To create a template config, run this with: {}", CREATE_CONFIG_COMMAND);
            throw e;
        }
    }

    private static Configuration createConfiguration(SmashbotConfig smashbotConfig, DatabaseClient databaseClient, RestClient restClient) {
        return new Configuration.Builder()
                .setName(smashbotConfig.getIrcName())
                .addServer(smashbotConfig.getIrcServer())
                .addAutoJoinChannel(smashbotConfig.getIrcChannel())
                .addListener(new IrcEventListener(databaseClient, restClient))
                .buildConfiguration();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase(CREATE_CONFIG_COMMAND)) {
                createConfig();
            } else if (args[0].equalsIgnoreCase(UPDATE_CONFIG_COMMAND)) {
                updateConfig();
            }
            return;
        }

        // We want fail fast and to see it immediately by the bot disappearing
        // from IRC, so we terminate if any exceptions leak out on any thread.
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            log.error("Unexpected exception [thread: {}], reason: {}", thread.getName(), ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        });

        SmashbotConfig smashbotConfig = readSmashbotConfig();
        DatabaseClient databaseClient = new DatabaseClient(smashbotConfig.getDatabaseHostname());
        RestClient restClient = new RestClient(smashbotConfig);
        Configuration ircConfig = createConfiguration(smashbotConfig, databaseClient, restClient);

        IrcBot ircBot = new IrcBot(ircConfig);
        log.info("Connecting to server {} and channel {}.", smashbotConfig.getIrcServer(), smashbotConfig.getIrcChannel());
        ircBot.startBot();
    }
}
