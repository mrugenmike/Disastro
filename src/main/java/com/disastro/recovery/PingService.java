package com.disastro.recovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PingService {
    public boolean ping(String ip) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if(ip!=null){
            ProcessBuilder processBuilder = new ProcessBuilder("ping", isWindows? "-n" : "-c", "1",ip);
            Process proc = null;
            try {
                proc = processBuilder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int returnVal = 0;
            try {
                returnVal = proc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return returnVal==0;
        }
    return false;
    }
}
