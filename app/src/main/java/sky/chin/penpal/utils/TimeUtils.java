package sky.chin.penpal.utils;

/**
 * Created by sky on 08/07/2015.
 */
public class TimeUtils {

    public static String generateTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

}
