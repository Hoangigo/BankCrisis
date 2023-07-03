package borse;


import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.System;
/**
 * The Gateway class connects the
 * different parts of this distributed system
 * by communicating with sensors, adapters and servers.
 */
public class Borse  {
    /**
     * Holds all sensorHandlers of the gateway
     * which provide an interface for fetching
     * data from sensor
     */
    private final BankHandler[] handlers;

    /**
     * Periodically pulls data from the sensors.
     */
    private final Timer mainTimer;
    private final String borseName;
    public Borse(String borseName, BankHandler[] handlers) throws IOException {
        System.out.println(borseName + " starts");
        this.mainTimer = new Timer();
        this.handlers = handlers;
        this.borseName= borseName;
    }

    /**
     * Starts the mainTimer to periodically pull
     * data from the sensors.
     * @param delay Sets the delay for each pull.
     */
    public void startPullingData(int delay) {
        mainTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                broadcastToBanks();
            }
        }, 0, delay);
    }
    public void broadcastToBanks() {
        try {
            for (BankHandler handler : handlers) {
                if (handler != null) {
                    String msg = new Message().toString();
                    handler.sendMessage(msg);
                }
            }
        } catch (IOException ignored) {}
    }

}
