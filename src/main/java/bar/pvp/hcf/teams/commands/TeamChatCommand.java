package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.profiles.ProfileChatType;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamChatCommand extends TeamCommand {

    public TeamChatCommand() {}

    @Command(name = "f.c", aliases = {"team.c", "teams.c", "teams.chat", "f.chat", "team.chat"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        String[] args = command.getArgs();

        List<String> chatTypes = new ArrayList<>(Arrays.asList(
                "a", "ally", "f", "team", "p", "public"
        ));

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (args.length == 0 || (!chatTypes.contains(args[0]))) {
            ProfileChatType toToggle = getToToggle(profile);

            if (toToggle != ProfileChatType.PUBLIC && profile.getFaction() == null) {
                player.sendMessage(langConfig.getString("ERROR.MUST_BE_IN_FACTION_FOR_CHAT_TYPE"));
                return;
            }

            setChatType(player, toToggle);
            return;
        }

        String arg = args[0];
        if (arg.equalsIgnoreCase("public") || arg.equalsIgnoreCase("p")) {
            setChatType(player, ProfileChatType.PUBLIC);
            return;
        }

        if (profile.getFaction() == null) {
            player.sendMessage(langConfig.getString("ERROR.MUST_BE_IN_FACTION_FOR_CHAT_TYPE"));
            return;
        }

        if (arg.equalsIgnoreCase("a") || arg.equalsIgnoreCase("ally") || arg.equalsIgnoreCase("alliance")) {
            setChatType(player, ProfileChatType.ALLY);
            return;
        }

        if (arg.equalsIgnoreCase("f") || arg.equalsIgnoreCase("team"))
            setChatType(player, ProfileChatType.FACTION);

    }

    private ProfileChatType getToToggle(Profile profile) {
        if (profile.getFaction() == null && profile.getChatType() != ProfileChatType.PUBLIC) {
            return ProfileChatType.PUBLIC;
        }

        switch (profile.getChatType()) {
            case FACTION:
                return ProfileChatType.ALLY;
            case ALLY:
                return ProfileChatType.PUBLIC;
            case PUBLIC:
                return ProfileChatType.FACTION;
        }

        return null;
    }

    private void setChatType(Player player, ProfileChatType type) {
        final String ROOT = "FACTION_OTHER.CHAT_CHANGED.";

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setChatType(type);

        switch (type) {
            case PUBLIC: {
                player.sendMessage(langConfig.getString(ROOT + "PUBLIC"));
                break;
            }
            case FACTION: {
                player.sendMessage(langConfig.getString(ROOT + "FACTION"));
                break;
            }
            case ALLY: {
                player.sendMessage(langConfig.getString(ROOT + "ALLY"));
                break;
            }
        }
    }
}
