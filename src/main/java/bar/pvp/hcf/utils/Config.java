package bar.pvp.hcf.utils;

import bar.pvp.hcf.HCTeams;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class Config {

    @Getter
    private YamlConfiguration config;

    @Getter
    private File file;

    public Config(String name) {
        String realName = name.contains(".yml") ? name : name + ".yml";
        this.file = new File(HCTeams.getInstance().getDataFolder(), realName);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public String getString(String path) { return CC.translate(config.getString(path)); }
    public List<String> getStringList(String path) { return CC.translate(config.getStringList(path)); }
    public boolean getBoolean(String path) { return config.getBoolean(path); }
    public Integer getInteger(String path) { return config.getInt(path); }
    public Double getDouble(String path) { return config.getDouble(path); }

}
