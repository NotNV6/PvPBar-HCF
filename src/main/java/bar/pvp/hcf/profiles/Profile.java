package bar.pvp.hcf.profiles;

import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.classes.PvpClass;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.claims.ClaimPillar;
import bar.pvp.hcf.teams.claims.ClaimProfile;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.timers.type.PlayerTimer;
import bar.pvp.hcf.utils.TeamMongo;
import com.mongodb.client.MongoCollection;

import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Getter
@Setter
public class Profile {


    private static List<Profile> profiles = new ArrayList<>();

    private int economy, killStreak;
    private boolean viewingMap, inAdminMode;


    private Player player;
    private PlayerTeam faction;
    private ProfileChatType chatType;
    private UUID uuid;
    private ClaimProfile claimProfile;
    private Claim lastInside;

    // Lists and Sets
    private Set<ClaimPillar> mapPillars;
    private List<PlayerTimer> playerTimers;

    private PvpClass pvpClass;

    public Profile(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;

        this.load();

    }

    private void load() {
        profiles.add(this);

        mapPillars = new HashSet<>();
        playerTimers = new ArrayList<>();
        chatType = ProfileChatType.PUBLIC;

        PlayerTeam.getPlayerTeams().forEach(faction -> { if(faction.getAllPlayerUuids().contains(uuid)) this.faction = faction; });

        MongoCollection collection = TeamMongo.getProfiles();
        Document document = (Document) collection.find(eq("uuid", player.getUniqueId().toString())).first();
        if(document == null) {
            this.economy = HCTeams.getInstance().getConfig().getInt("FACTION_GENERAL.STARTING_BALANCE");
            player.sendMessage(HCTeams.getInstance().getLangConfig().getString("MESSAGES.GAINED_MONEY").replace("%AMOUNT%", this.economy + ""));
            return;
        }

        this.economy = document.getInteger("economy");

    }

    public void save() {
        Document document = new Document();

        document.put("uuid", player.getUniqueId().toString());
        document.put("economy", economy);

        TeamMongo.getProfiles().replaceOne(eq("uuid", player.getUniqueId().toString()), document, new UpdateOptions().upsert(true));
    }

    @SuppressWarnings("all")
    public String getByLocation() {
        String returnString;
        Team teamToReturn;

        if (Claim.getProminentClaimAt(player.getLocation()) != null) {
            teamToReturn = Claim.getProminentClaimAt(player.getLocation()).getTeam();
            if (teamToReturn instanceof SystemTeam)
                returnString = ((SystemTeam) teamToReturn).getColor() + teamToReturn.getName();
            else
                returnString = (((PlayerTeam) teamToReturn).getMembers().contains(player.getUniqueId()) ? CC.RED : CC.GREEN) + teamToReturn.getName();
        } else {
            return "&7Wilderness";
        }
        return returnString;
    }

    public String getAstrix() {
        String astrix;

        if(faction.getLeader().equals(uuid))
            astrix = "**";
        else if(faction.getOfficers().contains(uuid))
            astrix = "*";
        else
            astrix = "";

        return astrix;
    }

    public PlayerTimer getByName(String name) { return playerTimers.stream().filter(timer -> timer.getName().equalsIgnoreCase(name)).findFirst().orElse(null); }
    public static Profile getByUuid(UUID uuid) { return profiles.stream().filter(profile -> profile.getUuid().equals(uuid)).findFirst().orElse(null); }
    public static Profile getProfile(Player player) { return profiles.stream().filter(profile -> profile.getPlayer().equals(player)).findFirst().orElse(null); }
    public static Collection<Profile> getProfiles() { return profiles; }
}
