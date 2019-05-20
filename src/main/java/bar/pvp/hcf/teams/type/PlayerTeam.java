package bar.pvp.hcf.teams.type;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.utils.TeamMongo;
import bar.pvp.hcf.utils.player.PlayerUtil;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

@Getter
@Setter
public class PlayerTeam extends Team {

    private UUID leader;
    private Set<UUID> officers, members;
    private Set<PlayerTeam> allies;
    private Set<UUID> requestedAllies;
    private BigDecimal deathsTillRaidable;
    private int[] freezeInformation;
    private Map<UUID, UUID> invitedPlayers;
    private int balance;

    public PlayerTeam(String name, UUID leader, UUID uuid) {
        super(name, uuid);

        this.leader = leader;

        officers = new HashSet<>();
        members = new HashSet<>();
        invitedPlayers = new HashMap<>();
        deathsTillRaidable = BigDecimal.valueOf(mainConfig.getDouble("FACTION_GENERAL.STARTING_DTR"));
        requestedAllies = new HashSet<>();
        allies = new HashSet<>();
    }

    public boolean isRaidable() {
        return getDeathsTillRaidable().doubleValue() <= 0;
    }

    public boolean isFrozen() {
        return freezeInformation != null;
    }

    public void freeze(int duration) {
        freezeInformation = new int[]{duration, (int) (System.currentTimeMillis() / 1000)};
    }

    public BigDecimal getMaxDeathsTillRaidable() {
        return BigDecimal.valueOf(mainConfig.getDouble("FACTION_GENERAL.STARTING_DTR") + mainConfig.getDouble("FACTION_GENERAL.DTR_PER_PLAYER") * getAllPlayerUuids().size());
    }

    public List<UUID> getAllPlayerUuids() {
        List<UUID> toReturn = new ArrayList<>();

        toReturn.add(leader);
        toReturn.addAll(officers);
        toReturn.addAll(members);

        return toReturn;
    }

    public void sendMessage(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public Set<Player> getOnlinePlayers() {
        Set<Player> toReturn = new HashSet<>();
        for (UUID uuid : getAllPlayerUuids()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                toReturn.add(player);
            }
        }
        return toReturn;
    }

    public String getFormattedFreezeDuration(){
        int timeLeft = (int) (getFreezeInformation()[0] + getFreezeInformation()[1] - (System.currentTimeMillis() / 1000));
        long hours = TimeUnit.SECONDS.toHours(timeLeft);
        long minutes = TimeUnit.SECONDS.toMinutes(timeLeft) - (hours * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(timeLeft) - ((hours * 60 * 60) + (minutes * 60));

        String formatted;
        if (hours == 0 && minutes > 0 && seconds > 0)
            formatted = minutes + " minutes and " + seconds + " seconds";
         else if (hours == 0 && minutes > 0 && seconds == 0)
            formatted = minutes + " minutes";
         else if (hours == 0 && minutes == 0 && seconds > 0)
            formatted = seconds + " seconds";
         else if (hours > 0 && minutes > 0 && seconds == 0)
            formatted = hours + " hours and " + minutes + " minutes";
         else if (hours > 0 && minutes == 0 && seconds > 0)
            formatted = hours + " hours and " + seconds + " seconds";
         else
            formatted = hours + " hours, " + minutes + " minutes and " + seconds + " seconds";


        if (hours == 1) formatted = formatted.replace("hours", "hour");
        if (minutes == 1) formatted = formatted.replace("minutes", "minute");
        if (seconds == 1) formatted = formatted.replace("seconds", "second");

        return formatted;
    }

    public Set<UUID> getAllyUuids() {
        Set<UUID> toReturn = new HashSet<>();
        for (PlayerTeam playerFaction : getAllies()) {
            toReturn.add(playerFaction.getUuid());
        }
        return toReturn;
    }

    public static PlayerTeam getByPlayer(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        return profile.getFaction();
    }

    public static PlayerTeam getByPlayerName(String name) {
        return getByPlayer(Bukkit.getPlayer(name));
    }

    public static Team getAnyByString(String factionName) {
        Team team = Team.getByName(factionName);

        if (team == null) {
            team = PlayerTeam.getByPlayerName(factionName);

            if (team == null) return null;

        }
        return team;
    }

    public static Set<PlayerTeam> getPlayerTeams() {
        Set<PlayerTeam> toReturn = new HashSet<>();
        getTeams().forEach(team -> {
            if(team instanceof PlayerTeam) toReturn.add((PlayerTeam) team);
        });
        return toReturn;
    }

    public static void load() {
        MongoCollection collection = TeamMongo.getPlayerTeams();
        Map<PlayerTeam, Set<UUID>> allyCache = new HashMap<>();
        collection.find().forEach((Block) obj -> {
            Document dbo = (Document) obj;

            UUID uuid = UUID.fromString(dbo.getString("uuid"));
            UUID leader = UUID.fromString(dbo.getString("leader"));
            String name = dbo.getString("name");

            double dtr = dbo.getDouble("dtr");
            int balance = dbo.getInteger("balance");
            int[] freezeInformation = null;
            String home = null;
            String announcement = null;

            Set<UUID> officers = new HashSet<>();
            Set<UUID> members = new HashSet<>();
            Map<UUID, UUID> invitedPlayers = new HashMap<>();
            if (dbo.containsKey("freezeLength") && dbo.containsKey("freezeInit")) {
                freezeInformation = new int[]{dbo.getInteger("freezeLength"), dbo.getInteger("freezeInit")};
            }

            if (dbo.containsKey("home")) {
                home = dbo.getString("home");
            }

            if (dbo.containsKey("announcement")) {
                announcement = dbo.getString("announcement");
            }

            Document invitedPlayerMap = (Document) dbo.get("invitedPlayers");
            for (String key : invitedPlayerMap.keySet()) {
                UUID invitedPlayer = UUID.fromString(key);
                UUID invitedBy = (UUID) invitedPlayerMap.get(key);
                invitedPlayers.put(invitedPlayer, invitedBy);
            }

            List<String> membersList = (List<String>) dbo.get("members");
            for (String member : membersList) {
                if (member.length() == uuid.toString().length()) {
                    members.add(UUID.fromString(member));
                }
            }

            List<String> officerList = (List<String>) dbo.get("officers");
            for (String officer : officerList) {
                if (officer.length() == uuid.toString().length()) {
                    officers.add(UUID.fromString(officer));
                }
            }

            final PlayerTeam playerFaction = new PlayerTeam(name, leader, uuid);
            playerFaction.setOfficers(officers);
            playerFaction.setMembers(members);
            playerFaction.setBalance(balance);
            playerFaction.setDeathsTillRaidable(BigDecimal.valueOf(dtr));
            playerFaction.setFreezeInformation(freezeInformation);
            playerFaction.setHome(home);
            playerFaction.setInvitedPlayers(invitedPlayers);
            playerFaction.setAnnouncement(announcement);

            List<String> claims = (List<String>) dbo.get("claims");
            for (String claim : claims) {
                if (claim.length() >= 5) {
                    String[] claimParts = (claim).split(";");
                    final String worldName = claimParts[0];
                    final int x1 = Integer.parseInt(claimParts[1]);
                    final int z1 = Integer.parseInt(claimParts[2]);
                    final int x2 = Integer.parseInt(claimParts[3]);
                    final int z2 = Integer.parseInt(claimParts[4]);
                    new Claim(playerFaction, new int[]{x1, x2, z1, z2}, worldName);
                }
            }

            List<String> allies = (List<String>) dbo.get("allies");
            allies.forEach(ally -> {
                if (ally.length() == uuid.toString().length()) {
                    if (allyCache.containsKey(playerFaction))
                        allyCache.get(playerFaction).add(UUID.fromString(ally));
                    else
                        allyCache.put(playerFaction, new HashSet<>(Arrays.asList(UUID.fromString(ally))));

                }
            });
        });

        for (PlayerTeam key : allyCache.keySet()) {
            for (UUID allyUuid : allyCache.get(key)) {
                Team allyTeam = Team.getByUuid(allyUuid);
                if (allyTeam instanceof PlayerTeam) {
                    key.getAllies().add((PlayerTeam) allyTeam);
                }
            }
        }

        for (Player player : PlayerUtil.getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            Claim claim = Claim.getProminentClaimAt(player.getLocation());
            if (claim != null) {
                profile.setLastInside(claim);
            }
            PlayerTeam playerFaction = PlayerTeam.getByPlayerName(player.getName());
            if (profile.getFaction() == null && playerFaction != null) {
                profile.setFaction(playerFaction);
            }
        }
    }

    public void saveTeam() {
        MongoCollection collection = TeamMongo.getPlayerTeams();
        getPlayerTeams().forEach(playerTeam -> {
            PlayerTeam playerFaction = playerTeam;

            Document dbo = new Document();

            dbo.put("uuid", playerFaction.getUuid().toString());
            dbo.put("leader", playerFaction.getLeader().toString());
            dbo.put("name", playerFaction.getName());
            dbo.put("name_lower", playerFaction.getName().toLowerCase());
            dbo.put("dtr", playerFaction.getDeathsTillRaidable().doubleValue());
            dbo.put("balance", playerFaction.getBalance());

            if (playerFaction.isFrozen()) {
                dbo.put("freezeLength", playerFaction.getFreezeInformation()[0]);
                dbo.put("freezeInit", playerFaction.getFreezeInformation()[1]);
            }

            if (playerFaction.getHome() != null) {
                dbo.put("home", playerFaction.getHome());
            }

            if (playerFaction.getAnnouncement() != null) {
                dbo.put("announcement", playerFaction.getAnnouncement());
            }

            List<String> officers = new ArrayList<>();
            List<String> members = new ArrayList<>();
            List<String> allies = new ArrayList<>();
            List<String> requestedAllies = new ArrayList<>();
            Document invitedPlayers = new Document();
            List<String> claims = new ArrayList<>();

            officers.addAll(PlayerUtil.getConvertedUuidSet(playerFaction.getOfficers()));
            members.addAll(PlayerUtil.getConvertedUuidSet(playerFaction.getMembers()));
            allies.addAll(PlayerUtil.getConvertedUuidSet(playerFaction.getAllyUuids()));
            requestedAllies.addAll(PlayerUtil.getConvertedUuidSet(playerFaction.getRequestedAllies()));

            playerFaction.getInvitedPlayers().keySet().forEach(player -> invitedPlayers.put(player.toString(), playerFaction.getInvitedPlayers().get(player)));
            playerFaction.getClaims().forEach(claim -> claims.add(claim.getWorldName() + ';' + claim.getFirstX() + ';' + claim.getFirstZ() + ';' + claim.getSecondX() + ';' + claim.getSecondZ()));


            dbo.put("officers", officers);
            dbo.put("members", members);
            dbo.put("allies", allies);
            dbo.put("requestedAllies", requestedAllies);
            dbo.put("invitedPlayers", invitedPlayers);
            dbo.put("claims", claims);


            collection.replaceOne(eq("uuid", playerFaction.getUuid().toString()), dbo, new UpdateOptions().upsert(true));
        });
    }

    public static void runTasks(HCTeams plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerTeam playerFaction : PlayerTeam.getPlayerTeams()) {
                    if (playerFaction.getDeathsTillRaidable().doubleValue() > playerFaction.getMaxDeathsTillRaidable().doubleValue()) {
                        playerFaction.setDeathsTillRaidable(playerFaction.getMaxDeathsTillRaidable());
                    }
                    if (playerFaction.getDeathsTillRaidable().doubleValue() < plugin.getMainConfig().getDouble("FACTION_GENERAL.MIN_DTR")) {
                        playerFaction.setDeathsTillRaidable(BigDecimal.valueOf(plugin.getMainConfig().getDouble("FACTION_GENERAL.MIN_DTR")));
                    }
                    if (playerFaction.isFrozen()) {
                        if (System.currentTimeMillis() / 1000 - playerFaction.getFreezeInformation()[1] >= playerFaction.getFreezeInformation()[0]) {
                            playerFaction.setFreezeInformation(null);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerTeam playerFaction : PlayerTeam.getPlayerTeams()) {
                    if (!(playerFaction.isFrozen()) && playerFaction.getDeathsTillRaidable().doubleValue() < playerFaction.getMaxDeathsTillRaidable().doubleValue()) {
                        playerFaction.setDeathsTillRaidable(playerFaction.getDeathsTillRaidable().add(BigDecimal.valueOf(0.1)));
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, plugin.getMainConfig().getInteger("FACTION_GENERAL.REGEN_DELAY") * 20, plugin.getMainConfig().getInteger("FACTION_GENERAL.REGEN_DELAY") * 20);
    }

}
