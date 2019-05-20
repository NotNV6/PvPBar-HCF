package bar.pvp.hcf.teams;

import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.utils.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@SuppressWarnings("deprecation")
@Getter
public class Team {

    private static Set<Team> teams = new HashSet<>();
    private static HCTeams main = HCTeams.getInstance();

    public Config mainConfig = main.getMainConfig();

    @Setter
    private String name, home, announcement;

    private UUID uuid;
    private Set<Claim> claims;

    public Team(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;

        claims = new HashSet<>();

        if (uuid == null) this.uuid = UUID.randomUUID();

        teams.add(this);
    }


    public static Team getByName(String name) { return getTeams().stream().filter(team -> team.getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", ""))).findFirst().orElse(null); }

    public static Team getByUuid(UUID uuid) { return getTeams().stream().filter(team -> team.getUuid().equals(uuid)).findFirst().orElse(null); }

    public static Set<Team> getAllByString(String string) {
        Set<Team> toReturn = new HashSet<>();
        for (Team team : teams) {
            if (!(toReturn.contains(team))) {

                if (team.getName().replace(" ", "").equalsIgnoreCase(string)) toReturn.add(team);


                if (team instanceof PlayerTeam) {
                    PlayerTeam playerFaction = (PlayerTeam) team;

                    for (UUID uuid : playerFaction.getAllPlayerUuids()) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        if (offlinePlayer != null && offlinePlayer.getName().equalsIgnoreCase(string)) {
                            toReturn.add(team);
                        }
                    }
                }

            }
        }
        return toReturn;
    }

    public boolean isNearBorder(Location l) {
        for (Claim claim : getClaims()) {
            if (claim.getWorldName().equals(l.getWorld().getName())) {
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(0, 0, 1))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(1, 0, 0))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(0, 0, -1))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(-1, 0, 0))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(-1, 0, 1))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(-1, 0, -1))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(1, 0, 1))) return true;
                if (claim.isInside(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()).add(1, 0, -1))) return true;

            }
        }
        return false;
    }

    public static ItemStack getWand(HCTeams main) {
        return new ItemBuilder(Material.valueOf(main.getMainConfig().getString("FACTION_CLAIMING.WAND.TYPE")))
                .lore(main.getMainConfig().getStringList("FACTION_CLAIMING.WAND.LORE"))
                .displayName(main.getMainConfig().getString("FACTION_CLAIMING.WAND.NAME")).create();
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        PlayerTeam.load();
        SystemTeam.load();
        main.setLoaded(true);
    }

    public static void save() {
        if (!(getTeams().isEmpty())) {
            System.out.println("Preparing to save " + getTeams().size() + " team" + (getTeams().size() > 1 ? 's' : "") + '.');

            if(!PlayerTeam.getPlayerTeams().isEmpty()) PlayerTeam.getPlayerTeams().forEach(PlayerTeam::saveTeam);
            if(!SystemTeam.getSystemTeams().isEmpty()) SystemTeam.getSystemTeams().forEach(SystemTeam::saveTeam);
        }

    }

    public static Set<Team> getTeams() {
        return teams;
    }
}