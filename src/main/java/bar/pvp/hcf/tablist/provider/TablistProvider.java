package bar.pvp.hcf.tablist.provider;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.tablist.TablistEntrySupplier;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.utils.LocationSerialization;
import bar.pvp.hcf.utils.MathHelper;
import bar.pvp.hcf.utils.player.PlayerUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class TablistProvider implements TablistEntrySupplier {

    public Table<Integer, Integer, String> getEntries(Player player) {
        Table<Integer, Integer, String> tab = HashBasedTable.create();

        Profile profile = Profile.getProfile(player);

        if(profile.getFaction() == null) {
            tab.put(0, 0, CC.DARK_AQUA + "Player Info");
            tab.put(0, 1, CC.WHITE + "Kills: " + player.getStatistic(Statistic.PLAYER_KILLS));
            tab.put(0, 2, CC.WHITE + "Deaths: " + player.getStatistic(Statistic.DEATHS));

            tab.put(0, 4, CC.DARK_AQUA + "Location");
            tab.put(0, 5, CC.translate(profile.getByLocation()));
            tab.put(0, 6, CC.WHITE + "(" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ() + ") " + CC.GRAY + "[" + getDirection(player) + ']');
        } else {
            String home;
            PlayerTeam team = profile.getFaction();
            if(profile.getFaction() == null || profile.getFaction().getHome() == null) home = "Not Set";
            else {
                Location homeLocation = LocationSerialization.deserializeLocation(team.getHome());
                home = homeLocation.getBlockX() + ", " + homeLocation.getBlockZ();
            }

            tab.put(0, 0, CC.DARK_AQUA + "Home");
            tab.put(0, 1, CC.WHITE + "" + home);

            tab.put(0, 3, CC.DARK_AQUA + "Faction Info");
            tab.put(0, 4, CC.WHITE + "Online: " + team.getOnlinePlayers().size() + '/' + team.getAllPlayerUuids().size());
            tab.put(0, 5, CC.WHITE + "DTR: " + team.getDeathsTillRaidable());
            tab.put(0, 6, CC.WHITE + "Balance: $" + team.getBalance());

            tab.put(0, 8, CC.DARK_AQUA + "Player Info");
            tab.put(0, 9, CC.WHITE + "Kills: " + player.getStatistic(Statistic.PLAYER_KILLS));
            tab.put(0, 10, CC.WHITE + "Deaths: " + player.getStatistic(Statistic.DEATHS));

            tab.put(0, 12, CC.DARK_AQUA + "Location");
            tab.put(0, 13, CC.translate(profile.getByLocation()));
            tab.put(0, 14, CC.WHITE + "(" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ() + ") " + CC.GRAY + "[" + getDirection(player) + ']');
        }

        tab.put(1, 0, CC.DARK_AQUA + CC.BOLD.toString() + "PvPBar");

        if(profile.getFaction() != null) {
            PlayerTeam team = profile.getFaction();

            tab.put(1, 2, CC.DARK_GREEN + team.getName());
            int h = 3;
            for(Player target : team.getOnlinePlayers()) {
                Profile targetP = Profile.getProfile(target);
                if(h >= 18) tab.put(1, 19, "&7&oAnd &e" + (team.getAllPlayerUuids().size()-h) + " more..."); else
                tab.put(1, h, CC.GREEN + targetP.getAstrix() + player.getName());
                h++;
            }
        }


        tab.put(2, 0, CC.DARK_AQUA + "End Portals");
        tab.put(2, 1, CC.WHITE + "1000, 1000");
        tab.put(2, 2, CC.WHITE + "in each quadrant");

        tab.put(2, 4, CC.DARK_AQUA + "Map Kit");
        tab.put(2, 5, CC.WHITE + "Sharpness I");
        tab.put(2, 6, CC.WHITE + "Protection II");

        tab.put(2, 8, CC.DARK_AQUA + "Border");
        tab.put(2, 9, CC.WHITE + "1,250x1,250");

        tab.put(2, 11, CC.DARK_AQUA + "Online Players");
        tab.put(2, 12, CC.WHITE + "" + PlayerUtil.getOnlinePlayers().size() + '/' + Bukkit.getMaxPlayers());

        return tab;
    }

    private String getDirection(Player player) {
        String dir = "";

        float angle = MathHelper.wrapAngleTo180_float(player.getLocation().getYaw());
        angle += 180.0F;

        if(angle >= 0F && angle <= 360F) dir = "N";
        if(angle >= 22.5F && angle <= 67.5F) dir = "NE";
        if(angle >= 67.5F && angle <= 112.5F) dir = "E";
        if(angle >= 112.5F && angle <= 157.5F) dir = "SE";
        if(angle >= 157.5F && angle <= 202.5F) dir = "S";
        if(angle >= 202.5F && angle <= 247.5F) dir = "SW";
        if(angle >= 247.5F && angle <= 292.45F) dir = "W";
        if(angle >= 292.45F && angle <= 337.5f) dir = "NW";

        return dir;
    }

    public String getHeader(Player player) { return ""; }

    public String getFooter(Player player) { return ""; }
}
