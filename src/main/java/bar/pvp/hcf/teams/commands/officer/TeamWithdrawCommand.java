package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.economy.Economy;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;

public class TeamWithdrawCommand extends TeamCommand {

    private Economy economy = main.getEconomy();

    @Command(name = "f.withdraw", aliases = {"team.withdraw", "teams.withdraw", "f.w", "team.w", "teams.w"}, inFactionOnly = true, isOfficerOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.WITHDRAW"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();
        int amount;

        if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("a")) {
            amount = playerFaction.getBalance();
        } else {
            if (!(NumberUtils.isNumber(args[0]))) {
                player.sendMessage(langConfig.getString("ERROR.INVALID_NUMBER").replace("%STRING%", args[0]));
                return;
            }

            amount = (int) Math.floor(Double.valueOf(args[0]));

            if (amount > playerFaction.getBalance()) {
                player.sendMessage(langConfig.getString("ERROR.FACTION_NOT_ENOUGH_MONEY"));
                return;
            }
        }

        if (amount <= 0) {
            player.sendMessage(langConfig.getString("ERROR.INVALID_WITHDRAW_AMOUNT"));
            return;
        }

        economy.addBalance(profile, amount);

        playerFaction.setBalance(playerFaction.getBalance() - amount);
        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_WITHDRAW_MONEY").replace("%PLAYER%", player.getName()).replace("%AMOUNT%", amount + ""));
    }
}
