import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Logger logger = new BasicLogger(LogLevel.INFO);
        logger.debug("Hey this is a debug log");
        logger.info(Map.of("LOG", "INFO"));
        logger.error("this is an error log");
        logger.warn(List.of("This is a list!"));
        System.out.println("Hello world!");
    }
    public String convertDateFromOneFormatToOther(String date, String fromFormat, String toFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fromFormat);
        try {
            Date givenDate = sdf.parse(date);
            return new SimpleDateFormat(toFormat).format(givenDate);
        } catch (Exception e) {
            return "false";
        }
    }
}