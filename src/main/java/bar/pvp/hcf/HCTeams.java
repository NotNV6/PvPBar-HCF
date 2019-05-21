package bar.pvp.hcf;

import bar.pvp.hcf.classes.PvpClass;
import bar.pvp.hcf.listeners.ChatListeners;
import bar.pvp.hcf.listeners.TimerListeners;
import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.listeners.ProfileListeners;
import bar.pvp.hcf.scoreboard.Query;
import bar.pvp.hcf.scoreboard.provider.QueryProvider;
import bar.pvp.hcf.tablist.TablistManager;
import bar.pvp.hcf.tablist.provider.TablistProvider;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.ClaimListeners;
import bar.pvp.hcf.teams.claims.ClaimPillar;
import bar.pvp.hcf.teams.commands.*;
import bar.pvp.hcf.teams.commands.admin.TeamAdminCommand;
import bar.pvp.hcf.teams.commands.admin.TeamFreezeCommand;
import bar.pvp.hcf.teams.commands.admin.TeamSetDtrCommand;
import bar.pvp.hcf.teams.commands.admin.TeamThawCommand;
import bar.pvp.hcf.teams.commands.leader.TeamDemoteCommand;
import bar.pvp.hcf.teams.commands.leader.TeamDisbandCommand;
import bar.pvp.hcf.teams.commands.leader.TeamLeaderCommand;
import bar.pvp.hcf.teams.commands.leader.TeamPromoteCommand;
import bar.pvp.hcf.teams.commands.officer.*;
import bar.pvp.hcf.teams.commands.system.TeamColorCommand;
import bar.pvp.hcf.teams.commands.system.TeamCreateSystemCommand;
import bar.pvp.hcf.teams.commands.system.TeamToggleDeathbanCommand;
import bar.pvp.hcf.teams.economy.Economy;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.utils.command.CommandFramework;
import bar.pvp.hcf.utils.player.PlayerUtil;

import bar.pvp.hcf.timers.TimerManager;

import bar.pvp.hcf.utils.TeamMongo;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import org.bukkit.Bukkit;

import org.bukkit.plugin.java.JavaPlugin;



@Getter
@Setter
public class HCTeams extends JavaPlugin {

    @Getter
    private static HCTeams instance;

    private CommandFramework framework;
    private Config mainConfig, langConfig, scoreboardConfig;
    private Economy economy = new Economy();
    @Setter private boolean loaded;


    @Override
    public void onEnable() {
        MinecraftServer.getServer().setMotd(CC.translate("&3&lPvPBar &7&l｜ &fEU Proxy\n" + "&fDevelopment Server &7(More information soon)"));

        instance = this;

        registerConfiguration();

        framework = new CommandFramework(this);

        new TeamMongo(this, this.getConfig().getBoolean("DATABASE.AUTH.ENABLED"));
        new TablistManager(this, new TablistProvider(), (long) 0.8);
        new Query(this, new QueryProvider(), (long) 0.5);


        new TimerManager();

        Team.load();
        PvpClass.setup();

        PlayerTeam.runTasks(this);

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        Team.save();

        Profile.getProfiles().forEach(Profile::save);

        PlayerUtil.getOnlinePlayers().forEach(player -> {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if(profile.getClaimProfile() != null) profile.getClaimProfile().removePillars();
            profile.getMapPillars().forEach(ClaimPillar::remove);
        });


        TeamMongo.getClient().close();
    }

    private void registerCommands() {
        new TeamHelpCommand();
        new TeamDisbandCommand();
        new TeamCreateCommand();
        new TeamInviteCommand();
        new TeamJoinCommand();
        new TeamRenameCommand();
        new TeamPromoteCommand();
        new TeamDemoteCommand();
        new TeamLeaderCommand();
        new TeamUninviteCommand();
        new TeamChatCommand();
        new TeamSetHomeCommand();
        new TeamMessageCommand();
        new TeamAnnouncementCommand();
        new TeamLeaveCommand();
        new TeamShowCommand();
        new TeamKickCommand();
        new TeamInvitesCommand();
        new TeamAllyCommand();
        new TeamEnemyCommand();
        new TeamDepositCommand();
        new TeamWithdrawCommand();
        new TeamClaimCommand();
        new TeamMapCommand();
        new TeamUnclaimCommand();
        new TeamListCommand();
        new TeamHomeCommand();

        new TeamFreezeCommand();
        new TeamThawCommand();
        new TeamSetDtrCommand();
        new TeamAdminCommand();

        new TeamCreateSystemCommand();
        new TeamToggleDeathbanCommand();
        new TeamColorCommand();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new TimerListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ProfileListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ClaimListeners(), this);
    }

    private void registerConfiguration() {
        mainConfig = new Config("config");
        langConfig = new Config("messages");
        scoreboardConfig = new Config("scoreboard");

        this.kitMap = mainConfig.getBoolean("FACTION_GENERAL.KITMAP");
    }


    private boolean kitMap;
}
