package bar.pvp.hcf.utils;

public class MathHelper {

    public static float wrapAngleTo180_float(float f) {
        f %= 360.0F;

        if (f >= 180.0F) f -= 360.0F;
        if (f < -180.0F) f += 360.0F;
        return f;
    }
}
