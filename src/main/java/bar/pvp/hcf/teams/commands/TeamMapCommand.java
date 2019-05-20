package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.claims.ClaimPillar;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamMapCommand extends TeamCommand {
    @Command(name = "f.map", aliases = {"team.map", "teams.map"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isViewingMap()) {
            for (ClaimPillar claimPillar : profile.getMapPillars()) {
                claimPillar.remove();
            }
            profile.getMapPillars().clear();
            player.sendMessage(langConfig.getString("FACTION_MAP.MAP_REMOVED"));
            profile.setViewingMap(false);
            return;
        }

        Set<Claim> toDisplay = new HashSet<>();
        int[] pos = new int[]{player.getLocation().getBlockX(), player.getLocation().getBlockZ()};

        for (int x = pos[0] - 64; x < pos[0] + 64; x++) {
            for (int z = pos[1] - 64; z < pos[1] + 64; z++) {
                Location location = new Location(player.getWorld(), x, 0, z);
                ArrayList<Claim> claims = Claim.getClaimsAt(location);
                if (claims != null) {
                    for (Claim claim : claims) {
                        if (claim.getWorldName().equalsIgnoreCase(location.getWorld().getName())) {
                            toDisplay.add(claim);
                        }
                    }
                }
            }
        }

        if (toDisplay.isEmpty()) {
            player.sendMessage(langConfig.getString("ERROR.NO_CLAIMS_NEARBY"));
            return;
        }

        Map<Team, Material> shown = new HashMap<>();
        for (Claim claim : toDisplay) {
            Team team = claim.getTeam();
            Material material;
            if (team == profile.getFaction()) {
                if (mainConfig.getString("FACTION_MAP.PILLAR.FRIENDLY").equalsIgnoreCase("RANDOM")) {
                   material = Claim.getMapBlocks().get(new Random().nextInt(Claim.getMapBlocks().size()));
                } else {
                    material = Material.valueOf(mainConfig.getString("FACTION_MAP.PILLAR.FRIENDLY"));
                }
            } else if (profile.getFaction() != null && team instanceof PlayerTeam && profile.getFaction().getAllies().contains(team)) {
                if (mainConfig.getString("FACTION_MAP.PILLAR.ALLY").equalsIgnoreCase("RANDOM")) {
                    material = Claim.getMapBlocks().get(new Random().nextInt(Claim.getMapBlocks().size()));
                } else {
                    material = Material.valueOf(mainConfig.getString("FACTION_MAP.PILLAR.ALLY"));
                }
            } else if (!(team instanceof PlayerTeam)) {
                SystemTeam systemFaction = (SystemTeam) team;
                if (systemFaction.isDeathban()) {
                    if (mainConfig.getString("FACTION_MAP.PILLAR.SYSTEM_FACTION.DEATHBAN").equalsIgnoreCase("RANDOM")) {
                        material = Claim.getMapBlocks().get(new Random().nextInt(Claim.getMapBlocks().size()));
                    } else {
                        material = Material.valueOf(mainConfig.getString("FACTION_MAP.PILLAR.SYSTEM_FACTION.DEATHBAN"));
                    }
                } else {
                    if (mainConfig.getString("FACTION_MAP.PILLAR.SYSTEM_FACTION.NON-DEATHBAN").equalsIgnoreCase("RANDOM")) {
                        material = Claim.getMapBlocks().get(new Random().nextInt(Claim.getMapBlocks().size()));
                    } else {
                        material = Material.valueOf(mainConfig.getString("FACTION_MAP.PILLAR.SYSTEM_FACTION.NON-DEATHBAN"));
                    }
                }
            } else {
                if (mainConfig.getString("FACTION_MAP.PILLAR.ENEMY").equalsIgnoreCase("RANDOM")) {
                    material = Claim.getMapBlocks().get(new Random().nextInt(Claim.getMapBlocks().size()));
                } else {
                    material = Material.valueOf(mainConfig.getString("FACTION_MAP.PILLAR.ENEMY"));
                }
            }
            if (!(shown.containsKey(team))) {
                shown.put(claim.getTeam(), material);

                for (Location corner : claim.getCorners()) {
                    profile.getMapPillars().add(new ClaimPillar(player, corner).show(material, 0));
                }

                String name = material.name().toLowerCase();
                name = name.replace("_", " ");
                String[] segments = name.split(" ");
                name = "";
                for (String segment : segments) {
                    segment = segment.substring(0, 1).toUpperCase() + segment.substring(1, segment.length());
                    if (name.equals("")) {
                        name = segment;
                    } else {
                        name = name + " " + segment;
                    }
                }

                if (team instanceof PlayerTeam) {
                    if (profile.getFaction() == team) {
                        player.sendMessage(langConfig.getString("FACTION_MAP.DISPLAY.FRIENDLY").replace("%BLOCK%", name).replace("%FACTION%", team.getName()));
                    } else if (profile.getFaction() != null && profile.getFaction().getAllies().contains(team)) {
                        player.sendMessage(langConfig.getString("FACTION_MAP.DISPLAY.ALLY").replace("%BLOCK%", name).replace("%FACTION%", team.getName()));
                    } else {
                        player.sendMessage(langConfig.getString("FACTION_MAP.DISPLAY.ENEMY").replace("%BLOCK%", name).replace("%FACTION%", team.getName()));
                    }
                } else {
                    player.sendMessage(langConfig.getString("FACTION_MAP.DISPLAY.SYSTEM_FACTION").replace("%BLOCK%", name).replace("%FACTION%", team.getName()).replace("%COLOR%", ((SystemTeam) team).getColor() + ""));
                }
            } else {
                for (Location corner : claim.getCorners()) {
                    profile.getMapPillars().add(new ClaimPillar(player, corner).show(shown.get(claim.getTeam()), 0));
                }
            }
        }

        profile.setViewingMap(true);
    }
}
