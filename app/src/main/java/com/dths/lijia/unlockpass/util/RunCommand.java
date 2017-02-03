package com.dths.lijia.unlockpass.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by lijia on 2017/1/26.
 */

public class RunCommand {
    public static String run(String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("su");
        Process proc = builder.start();
        DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
        dos.writeBytes(command + "\n");
        dos.flush();
        dos.writeBytes("exit\n");
        dos.flush();
        try {
            if(proc.waitFor()!=0){
                throw new IOException("Seems no Root,code:"+proc.waitFor());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.toString());
        }
        DataInputStream dis = new DataInputStream(proc.getInputStream());

        StringBuilder s = new StringBuilder();
        String str;
        while ((str = dis.readLine()) != null) {
            s.append(str).append("\n");
        }

        return s.toString();
    }
    /*
    public static String readInput(Process proc) throws IOException {
        DataInputStream dis = new DataInputStream(proc.getInputStream());

        StringBuilder s = new StringBuilder();
        String str;
        while ((str = dis.readLine()) != null) {
            s.append(str).append("\n");
        }

        return s.toString();
    }

    public static String readError(Process proc) throws IOException {
        DataInputStream dis = new DataInputStream(proc.getErrorStream());

        StringBuilder s = new StringBuilder();
        String str;
        while ((str = dis.readLine()) != null) {
            s.append(str).append("\n");
        }

        return s.toString();
    }
    */
}
