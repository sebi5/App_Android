package sky.chin.penpal.utils;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimestampUtils {

    public static String generateTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

    public static String convertTimestampToText(String timestamp) {
        int current = Integer.parseInt(generateTimestamp());
        int time = Integer.parseInt(timestamp);

        int diff = current - time;

        if (diff < 60)
            return "Just Now";
        else if (diff < 3600)
            return (diff/60) + "mins ago";
        else if (diff < 86400)
            return (diff/3600) + "hrs ago";

        return getDate(time * 1000L);
    }

    private static String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

}
