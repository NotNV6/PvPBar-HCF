package bar.pvp.hcf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerialization {

    public static String serializeLocation(Location l) {
        String s = "";
        s = s + "@w;" + l.getWorld().getName();
        s = s + ":@x;" + l.getX();
        s = s + ":@y;" + l.getY();
        s = s + ":@z;" + l.getZ();
        s = s + ":@p;" + l.getPitch();
        s = s + ":@ya;" + l.getYaw();
        return s;
    }

    public static Location deserializeLocation(String s) {
        Location l = new Location(Bukkit.getWorlds().get(0), 0.0D, 0.0D, 0.0D);
        String[] att = s.split(":");
        int len = att.length;

        for(int i = 0; i < len; ++i) {
            String attribute = att[i];
            String[] split = attribute.split(";");
            if(split[0].equalsIgnoreCase("@w")) l.setWorld(Bukkit.getWorld(split[1]));
            if(split[0].equalsIgnoreCase("@x")) l.setX(Double.parseDouble(split[1]));

            if(split[0].equalsIgnoreCase("@y")) l.setY(Double.parseDouble(split[1]));
            if(split[0].equalsIgnoreCase("@z")) l.setZ(Double.parseDouble(split[1]));
            if(split[0].equalsIgnoreCase("@p")) l.setPitch(Float.parseFloat(split[1]));
            if(split[0].equalsIgnoreCase("@ya")) l.setYaw(Float.parseFloat(split[1]));

        }

        return l;
    }
}
