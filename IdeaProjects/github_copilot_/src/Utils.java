import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static class DateUtil {
        public static String getCurrentDate() {
            return new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
        }

        public static String isGivenDateBeforeCurrentDate(String date) {
            try {
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date givenDate = sdf.parse(date);
                Date currentDate = sdf.parse(getCurrentDate());
                return givenDate.before(currentDate) ? "true" : "false";
            } catch (Exception e) {
                return "false";
            }
        }

        public static String isGivenDateAfterCurrentDate(String date) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date givenDate = sdf.parse(date);
                Date currentDate = sdf.parse(getCurrentDate());
                return givenDate.after(currentDate) ? "true" : "false";
            } catch (Exception e) {
                return "false";
            }
        }

        public static String parseDate(String date, String format) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
                Date givenDate = sdf.parse(date);
                return new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(givenDate);
            } catch (Exception e) {
                return "false";
            }
        }
    }

    public static class MathUtils {
        public static int add(int a, int b) {
            return a + b;
        }

        public static int subtract(int a, int b) {
            return a - b;
        }

        public static int multiply(int a, int b) {
            return a * b;
        }

        public static int divide(int a, int b) {
            return a / b;
        }

        public static int remainder(int a, int b) {
            return a % b;
        }

        public static int power(int a, int b) {
            return (int) Math.pow(a, b);
        }

        public static int min(int a, int b) {
            return Math.min(a, b);
        }

        public static int max(int a, int b) {
            return Math.max(a, b);
        }

        public static int abs(int a) {
            return Math.abs(a);
        }

        public static int sqrt(int a) {
            return (int) Math.sqrt(a);
        }

        public static int cbrt(int a) {
            return (int) Math.cbrt(a);
        }

        public static int log(int a) {
            return (int) Math.log(a);
        }

        public static int log10(int a) {
            return (int) Math.log10(a);
        }

        public static int log2(int a) {
            return (int) (Math.log(a) / Math.log(2));
        }

        public static int floor(int a) {
            return (int) Math.floor(a);
        }

        public static int ceil(int a) {
            return (int) Math.ceil(a);
        }

        public static int round(int a) {
            return Math.round(a);
        }

        public static int random(int a) {
            return (int) (Math.random() * a);
        }

        public static int random(int a, int b) {
            return (int) (Math.random() * (b - a) + a);
        }

        public static int sum(int[] a) {
            int sum = 0;
            for (int i : a) {
                sum += i;
            }
            return sum;
        }

        public static int product(int[] a) {
            int product = 1;
            for (int i : a) {
                product *= i;
            }
            return product;
        }

        public static int min(int[] a) {
            int min = a[0];
            for (int i : a) {
                min = Math.min(min, i);
            }
            return min;
        }
    }

    public static class StringUtils {
        public static String reverse(String s) {
            return new StringBuilder(s).reverse().toString();
        }

        public static String capitalize(String s) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        public static String decapitalize(String s) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        }

        public static String isNullOrEmpty(String s) {
            return s == null || s.isEmpty() ? "true" : "false";
        }

        public static String[] split(String s, String regex) {
            return s.split(regex);
        }

        public static String[] splitOnWhitespace(String s) {
            return s.split("\\s+");
        }

    }
}