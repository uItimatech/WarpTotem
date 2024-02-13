package net.ultimatech.warptotem;

public class WTDependencyManager {
    public static boolean isEchoingWildsInstalled() {
        try {
            Class.forName("net.ultimatech.echoingwilds.EchoingWilds");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
