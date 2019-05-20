package bar.pvp.hcf.tablist;

import java.util.Arrays;
import java.util.UUID;


import bar.pvp.hcf.tablist.client.ClientVersion;
import bar.pvp.hcf.tablist.reflection.ReflectionConstants;
import bar.pvp.hcf.utils.player.PlayerUtil;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.spigotmc.ProtocolInjector;

import com.google.common.collect.Table;

import lombok.Getter;

public class Tablist {

    public static final Object[] GAME_PROFILES;
    public static final String[] TAB_NAMES;

    public static String[] BLANK_SKIN = {"eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=", "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw="};

    @Getter
    private final ClientVersion version;

    @Getter
    private final Player player;

    @Getter
    private boolean initiated;

    public Tablist(Player player) {
        this.player = player;
        this.version = ClientVersion.getVersion(player);
        addFakePlayers();
        update();
    }

    public void sendPacket(Player player, Object packet) {
        Object handle = ReflectionConstants.GET_HANDLE_METHOD.invoke(player);
        Object connection = ReflectionConstants.PLAYER_CONNECTION.get(handle);
        ReflectionConstants.SEND_PACKET.invoke(connection, packet);
    }

    public Tablist update() {
        TablistManager manager = TablistManager.INSTANCE;
        if (!initiated || manager == null) return this;
        Table<Integer, Integer, String> entries = manager.getSupplier().getEntries(player);
        boolean useProfiles = version.ordinal() != 0;
        int magic = useProfiles ? 4 : 3;
        if (useProfiles) {
            String header = manager.getSupplier().getHeader(player);
            if (header == null) {
                header = "";
            }
            String footer = manager.getSupplier().getFooter(player);
            if (footer == null) {
                footer = "";
            }
            ProtocolInjector.PacketTabHeader packet = new ProtocolInjector.PacketTabHeader(ChatSerializer.a("{text:\"" + StringEscapeUtils.escapeJava(header) + "\"}"), ChatSerializer.a("{text:\"" + StringEscapeUtils.escapeJava(footer) + "\"}"));
            sendPacket(player, packet);
        }
        for (int i = 0; i < (magic * 20); i ++) {
            int x = i % magic;
            int y = i / magic;
            String text = entries.get(x, y);
            if (text == null) {
                text = "";
            }
            String name = TAB_NAMES[i];
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
                return this;
            }
            player.setScoreboard(scoreboard);
            Team team = scoreboard.getTeam(name);
            if (team == null) {
                team = scoreboard.registerNewTeam(name);
            }
            if (!team.hasEntry(name)) {
                team.addEntry(name);
            }
            String prefix = "", suffix = "";
            if (text.length() < 17) {
                prefix = text;
            } else {
                String left = text.substring(0, 16), right = text.substring(16, text.length());
                if (left.endsWith("§")) {
                    left = left.substring(0, left.length() - 1);
                    right = "§" + right;
                }
                String last = ChatColor.getLastColors(left);
                right = last + right;
                prefix = left;
                suffix = StringUtils.left(right, 16);
            }
            team.setPrefix(prefix);
            team.setSuffix(suffix);
        }
        return this;
    }

    public Tablist hideRealPlayers() {
        if (!initiated) return this;
        boolean useProfiles = version.ordinal() != 0;
        PlayerUtil.getOnlinePlayers().forEach(other -> {
            if (!player.canSee(other)) return;
            Object packet = ReflectionConstants.TAB_PACKET_CONSTRUCTOR.invoke();
            if (useProfiles) {
                Object profile = ReflectionConstants.GET_PROFILE_METHOD.invoke(other);
                ReflectionConstants.TAB_PACKET_PROFILE.set(packet, profile);
            } else {
                ReflectionConstants.TAB_PACKET_NAME.set(packet, other.getName());
            }
            ReflectionConstants.TAB_PACKET_ACTION.set(packet, 4);
            sendPacket(player, packet);
        });
        return this;
    }

    public Tablist hideFakePlayers() {
        if (!initiated) return this;
        boolean useProfiles = version.ordinal() != 0;
        Arrays.stream(GAME_PROFILES).forEach(other -> {
            Object packet = ReflectionConstants.TAB_PACKET_CONSTRUCTOR.invoke();
            if (useProfiles) {
                ReflectionConstants.TAB_PACKET_PROFILE.set(packet, other);
            } else {
                String name = ReflectionConstants.GAME_PROFILE_NAME.get(other);
                ReflectionConstants.TAB_PACKET_NAME.set(packet, name);
            }
            ReflectionConstants.TAB_PACKET_ACTION.set(packet, 4);
            sendPacket(player, packet);
        });
        return this;
    }

    public Tablist addFakePlayers() {
        if (initiated) return this;
        boolean useProfiles = version.ordinal() != 0;
        int magic = useProfiles ? 4 : 3;
        for (int i = 0; i < (magic * 20); i ++) {
            int x = i % magic;
            int y = i / magic;
            Object packet = ReflectionConstants.TAB_PACKET_CONSTRUCTOR.invoke();
            Object profile = GAME_PROFILES[i];
            if (useProfiles) {
                ReflectionConstants.TAB_PACKET_PROFILE.set(packet, profile);
            } else {
                String name = ReflectionConstants.GAME_PROFILE_NAME.get(profile);
                ReflectionConstants.TAB_PACKET_NAME.set(packet, name);
            }
            ReflectionConstants.TAB_PACKET_ACTION.set(packet, 0);
            sendPacket(player, packet);
        }
        initiated = true;
        return this;
    }

    public void clear() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    static {
        GAME_PROFILES = new Object[80];
        TAB_NAMES = new String[80];
        for (int i = 0; i < 80; i ++) {
            int x = i % 4;
            int y = i / 4;
            String name = (x > 9 ? ("§" + String.valueOf(x).toCharArray()[0] + "§" + String.valueOf(x).toCharArray()[1]) : "§0§" + x) + (y > 9 ? ("§" + String.valueOf(y).toCharArray()[0] + "§" + String.valueOf(y).toCharArray()[1]) : "§0§" + String.valueOf(y).toCharArray()[0]);
            UUID id = UUID.randomUUID();
            Object profile = ReflectionConstants.GAME_PROFILE_CONSTRUCTOR.invoke(id, name);
            TAB_NAMES[i] = name;
            GAME_PROFILES[i] = profile;
        }
    }

}
