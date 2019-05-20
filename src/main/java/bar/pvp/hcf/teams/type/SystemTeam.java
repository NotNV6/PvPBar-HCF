package bar.pvp.hcf.teams.type;

import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.utils.TeamMongo;
import com.mongodb.BasicDBList;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Location;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Getter
@Setter
public class SystemTeam extends Team {

    private CC color = CC.WHITE;
    private Location middle;
    private boolean deathban = true;

    public SystemTeam(String name, UUID uuid) {
        super(name, uuid);
    }

    public static List<SystemTeam> getSystemTeams() {
        List<SystemTeam> systemTeams = new ArrayList<>();
        Team.getTeams().forEach(team -> {
            if (team instanceof SystemTeam)
                systemTeams.add((SystemTeam)team);

        });

        return systemTeams;
    }

    public static void load() {
        MongoCollection collection = TeamMongo.getSystemTeams();

        collection.find().forEach((Block) obj -> {
            Document dbo = (Document) obj;
            UUID uuid = UUID.fromString(dbo.getString("uuid"));
            String name = dbo.getString("name");

            final SystemTeam systemFaction = new SystemTeam(name, uuid);

            boolean deathban = dbo.getBoolean("deathban");
            CC color = CC.valueOf(dbo.getString("color"));

            if (dbo.containsKey("home")) systemFaction.setHome(dbo.getString("home"));


            if (dbo.containsKey("announcement")) systemFaction.setAnnouncement(dbo.getString("announcement"));


            systemFaction.setDeathban(deathban);
            systemFaction.setColor(color);

            List<String> claims = (List<String>) dbo.get("claims");
            for (String claim : claims) {
                if (claim.length() >= 5) {
                    String[] claimParts = (claim).split(";");
                    final String worldName = claimParts[0];
                    final int x1 = Integer.parseInt(claimParts[1]);
                    final int z1 = Integer.parseInt(claimParts[2]);
                    final int x2 = Integer.parseInt(claimParts[3]);
                    final int z2 = Integer.parseInt(claimParts[4]);
                    new Claim(systemFaction, new int[]{x1, x2, z1, z2}, worldName);
                }
            }
        });

    }

    public void saveTeam() {
        MongoCollection collection = TeamMongo.getSystemTeams();
        getSystemTeams().forEach(systemTeam -> {
            SystemTeam systemFaction = systemTeam;

            Document dbo = new Document();

            dbo.put("uuid", systemFaction.getUuid().toString());
            dbo.put("deathban", systemFaction.isDeathban());
            dbo.put("color", systemFaction.getColor().name());
            dbo.put("name", systemFaction.getName());
            dbo.put("name_lower", systemFaction.getName().toLowerCase());

            if (systemFaction.getAnnouncement() != null) dbo.put("announcement", systemFaction.getAnnouncement());


            if (systemFaction.getHome() != null) dbo.put("home", systemFaction.getHome());


            BasicDBList claims = new BasicDBList();
            systemFaction.getClaims().forEach(claim -> claims.add(claim.getWorldName() + ";" + claim.getFirstX() + ";" + claim.getFirstZ() + ";" + claim.getSecondX() + ";" + claim.getSecondZ()));

            dbo.put("claims", claims);

            collection.replaceOne(eq("uuid", systemFaction.getUuid().toString()), dbo, new UpdateOptions().upsert(true));
        });

    }

    public static SystemTeam getByName(String name) { return getSystemTeams().stream().filter(team -> team.getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", ""))).findFirst().orElse(null); }

}
