package bar.pvp.hcf.listeners;

import bar.pvp.hcf.profiles.ProfileChatType;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.utils.player.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ChatListeners implements Listener {

    private HCTeams main = HCTeams.getInstance();
    private Config mainConfig = main.getMainConfig();

    @EventHandler()
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (mainConfig.getBoolean("FACTION_CHAT.ENABLED")) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            List<Player> receipents = new ArrayList<>();

            int type;

            switch(profile.getChatType()) {
                case PUBLIC: {
                    PlayerUtil.getOnlinePlayers().forEach(receipents::add);
                } break;

                case ALLY: {
                    if(profile.getFaction() == null) {
                        profile.setChatType(ProfileChatType.PUBLIC);
                        PlayerUtil.getOnlinePlayers().forEach(receipents::add);
                    } else profile.getFaction().getAllies().forEach(ally -> ally.getMembers().forEach(member -> receipents.add(Bukkit.getPlayer(member))));
                    receipents.add(player);

                } break;

                case FACTION: {
                    if(profile.getFaction() == null) {
                        profile.setChatType(ProfileChatType.PUBLIC);
                        PlayerUtil.getOnlinePlayers().forEach(receipents::add);
                    } else profile.getFaction().getMembers().forEach(member -> receipents.add(Bukkit.getPlayer(member)));
                    receipents.add(player);

                } break;
            }


            receipents.forEach(target -> target.sendMessage(CC.translate(getFormat(profile, Profile.getProfile(target), player.getName(), event.getMessage()))));
        }
    }

    private String getFormat(Profile profile, Profile target, String name, String message) {
        String tag = null;

        switch(profile.getChatType()) {
            case FACTION: {
                tag = "&3(Team) " + name + ": " + CC.YELLOW + message;
            } break;

            case ALLY: {
                tag = "&9(Ally) " + name + ": " + CC.YELLOW + message;
            } break;

            case PUBLIC:
            default: {
                if(profile.getFaction() == null)
                    tag = mainConfig.getString("FACTION_CHAT.NO_FACTION") + CC.YELLOW + name + ": " + CC.WHITE + message;
                else
                {
                    if(target.getFaction() == null)
                        tag = mainConfig.getString("FACTION_CHAT.ENEMY").replace("%TAG%", profile.getFaction().getName()) + CC.YELLOW + name + ": " + CC.WHITE + message;
                    else if(target.getFaction().getAllies().contains(profile.getFaction()))
                        tag = mainConfig.getString("FACTION_CHAT.ALLY").replace("%TAG%", profile.getFaction().getName()) + CC.YELLOW + name + ": " + CC.WHITE +  message;
                    else if(target.getFaction().equals(profile.getFaction()))
                        tag = mainConfig.getString("FACTION_CHAT.FRIENDLY").replace("%TAG%", profile.getFaction().getName()) + CC.YELLOW + name + ": " + CC.WHITE + message;
                    else if(!target.getFaction().equals(profile.getFaction()))
                        tag = mainConfig.getString("FACTION_CHAT.ENEMY").replace("%TAG%", profile.getFaction().getName()) + CC.YELLOW + name + ": " + CC.WHITE + message;
                }
            } break;
        }

        return tag;
    }

}
