package smashbot.irc;

import org.pircbotx.Channel;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrcEventListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(IrcEventListener.class);
    private static final String IDENTIFIER_TOKEN = "!";

    private Channel channel;

    private void handleHelp(GenericMessageEvent event) {
        event.respondPrivateMessage("Usage:");
        event.respondPrivateMessage("!addmatch <winner> <loser> - Adds a match for the winner username to the DB against the loser username");
        event.respondPrivateMessage("!rank <username> - Gets ranking information for the user");
        event.respondPrivateMessage("!reminder - Requests that the bot sends out match reminders now instead of waiting until a daily timer fires");
    }

    private void handleMatch(String[] tokens) {
        // TODO
    }

    private void handleRank(String[] tokens) {
        // TODO
    }

    private void handleReminder(String[] tokens) {
        // TODO
    }

    @Override
    public void onJoin(JoinEvent event) {
        channel = event.getChannel();
        log.info("Joined channel " + channel.getName());
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if (channel == null || !event.getMessage().startsWith(IDENTIFIER_TOKEN))
            return;

        String[] tokens = event.getMessage().split("\\s");
        if (tokens.length == 0)
            return;

        String command = tokens[0].substring(1).toLowerCase();
        switch (command) {
        case "addmatch":
            handleMatch(tokens);
            break;
        case "help":
            handleHelp(event);
            break;
        case "rank":
            handleRank(tokens);
            break;
        case "reminder":
            handleReminder(tokens);
            break;
        default:
            break;
        }
    }
}
