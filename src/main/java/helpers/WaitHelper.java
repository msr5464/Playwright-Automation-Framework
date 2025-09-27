package helpers;

public class WaitHelper {

    public static void waitforseconds(Config config, int seconds) {
        try {
            config.logComment("Waiting for " + seconds + " seconds");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            config.logExceptionAndFail("Failed to wait for " + seconds + " seconds", e);
        }
    }
}