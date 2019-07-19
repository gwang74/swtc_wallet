package android.util;

public class Log {
    public static boolean isLoggable(String tag, int level) {
        return true;
    }

    public static int println(int priority, String tag, String msg) {
        return 0;
    }

    public static String getStackTraceString(Throwable tr) {
        return "";
    }
}
