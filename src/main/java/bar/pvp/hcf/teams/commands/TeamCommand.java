package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.HCTeams;


public class TeamCommand {

    public HCTeams main = HCTeams.getInstance();
    public Config langConfig = main.getLangConfig();
    public Config mainConfig = main.getMainConfig();

    public TeamCommand() {
        main.getFramework().registerCommands(this);
    }
}
