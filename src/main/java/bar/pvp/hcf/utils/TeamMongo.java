package bar.pvp.hcf.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class TeamMongo {

    @Getter
    private static MongoClient client;
    private MongoDatabase database;


    @Getter
    private static MongoCollection profiles, playerTeams, systemTeams;

    public TeamMongo(JavaPlugin plugin, boolean auth) {
        if(auth)
            client = new MongoClient(new ServerAddress(plugin.getConfig().getString("DATABASE.AUTH.HOST"), plugin.getConfig().getInt("DATABASE.AUTH.PORT")), Arrays.asList(MongoCredential.createCredential(plugin.getConfig().getString("DATABASE.AUTH.USER"), plugin.getConfig().getString("DATABASE.AUTH.DATABASE"), plugin.getConfig().getString("DATABASE.AUTH.PASSWORD").toCharArray())));
        else
            client = new MongoClient(new ServerAddress(plugin.getConfig().getString("DATABASE.AUTH.HOST"), plugin.getConfig().getInt("DATABASE.AUTH.PORT")));


        database = client.getDatabase(plugin.getConfig().getString("DATABASE.AUTH.DATABASE"));

        profiles = database.getCollection("profiles");
        playerTeams = database.getCollection("playerTeams");
        systemTeams = database.getCollection("systemTeams");
    }
}