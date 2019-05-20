package bar.pvp.hcf.utils;


import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static String getRemainingTime(float millis, boolean trailing) {
        DateFormat format = new SimpleDateFormat((millis >= TimeUnit.HOURS.toMillis(1L) ? "HH:" : "") + "mm:ss");
        NumberFormat formatter = new DecimalFormat("#0.0");
        return trailing ? formatter.format(millis/1000) : format.format(millis);
    }
}
