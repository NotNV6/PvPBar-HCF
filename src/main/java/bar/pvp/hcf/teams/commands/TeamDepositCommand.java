package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.economy.Economy;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;

public class TeamDepositCommand extends TeamCommand {

    private Economy economy = main.getEconomy();

    @Command(name = "f.deposit", aliases = {"team.deposit", "teams.deposit", "f.d", "team.d", "teams.d"}, inFactionOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.DEPOSIT"));
            return;
        }

        int amount;

        if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("*")) {
            amount = (int) Math.floor(economy.getBalance(profile));
        } else {
            if (!(NumberUtils.isNumber(args[0]))) {
                player.sendMessage(langConfig.getString("ERROR.INVALID_NUMBER").replace("%STRING%", args[0]));
                return;
            }

            amount = (int) Math.floor(Double.valueOf(args[0]));

            if (amount > economy.getBalance(profile)) {
                player.sendMessage(langConfig.getString("ERROR.NOT_ENOUGH_MONEY"));
                return;
            }
        }

        if (amount <= 0) {
            player.sendMessage(langConfig.getString("ERROR.INVALID_DEPOSIT_AMOUNT"));
            return;
        }

        economy.setBalance(profile, economy.getBalance(profile)-amount);

        PlayerTeam playerFaction = profile.getFaction();
        playerFaction.setBalance(playerFaction.getBalance() + amount);
        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_DEPOSIT_MONEY").replace("%PLAYER%", player.getName()).replace("%AMOUNT%", amount + ""));
    }
}
