package com.yaasoosoft.lfrida.presenter;

import android.util.Log;

import com.yaasoosoft.lfrida.model.Job;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JobThread implements Runnable{
    private final String homePath;
    private final String cachePath;
    private String injectPath ="";
    private final String TAG="RootShell";
    private final String suCmd="ppap";
    private final String key = "rKjpxpF7IqdseNcUHTyjSh4G8";
    private final String mima = "CxwI3tBtRFbilDwqQkuctxx5q";
    private boolean running=false;
    private LinkedList<String> outLog=new LinkedList<>();
    public Job job;
    public JobThread(Job job,String injectPath,String homePath,String cachePath)
    {
        this.injectPath=injectPath;
        this.homePath=homePath;
        this.cachePath=cachePath;
        this.job=job;
    }
    public String getName()
    {
        return job.getName();
    }
    private Process shell(List<String> command) {
        StringBuilder cmds= new StringBuilder();
        for (String cmd : command) {
            cmds.append(" ");
            cmds.append(cmd);
        }
        Log.e("shell", cmds.toString());
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(homePath));
        processBuilder.environment().put("HOME", homePath);
        processBuilder.environment().put("TMPDIR", cachePath);

        Process shellProcess=null;
        try {
            shellProcess=processBuilder.start();
        }catch (Exception e)
        {
            Log.e(TAG,e.toString());
        }
        return shellProcess;
    }
    private List<String> getRootCmd()
    {
        List<String>cmds=new ArrayList<>();
        int solt = (int) (System.currentTimeMillis() / 1000 / 3600 % 24);
        String pass = getPassWd(mima, key, solt);
        cmds.add(suCmd);
        cmds.add("-m");
        cmds.add(pass);
//        cmds.add("su");
        return cmds;
    }
    public String getPassWd(String message,String key,int solt)
    {
        char[] mm=message.toCharArray();
        char[] mk=key.toCharArray();
        for (int i = 0; i < mm.length; i++) {
            if(i%solt==0)
            {
                mm[i]=mk[i];
            }
        }
       String pass = DigestUtils.md5Hex(new String(mm));
        return pass;
    }
    public String getOutput()
    {
        String line=null;
        if(!outLog.isEmpty())
        {
            line=job.getName();
            line+=": ";
            line+=outLog.getFirst();
            outLog.removeFirst();
        }
        return line;
    }

    @Override
    public void run() {
        List<String> cmds=getRootCmd();
        cmds.add("-c");
        cmds.add(injectPath);
        cmds.add("-s");
        cmds.add(job.getJsPath());
        cmds.add("-f");
        cmds.add(job.getAppPackageName());
        Process process = shell(cmds);
        BufferedReader br=null;
        running=true;
        br=new BufferedReader(new InputStreamReader(process.getInputStream()));

        String outT="";
        try {
            if(process.getErrorStream().available()>0)
            {
                br=new BufferedReader(new InputStreamReader(process.getErrorStream()));
                outLog.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (running&&!Thread.interrupted()&& process.isAlive())
        {
            try {
                outT=br.readLine();
                if (outT!=null) {
                    outLog.add(outT);
                }
                else
                {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
                outLog.add(e.getMessage());
                running=false;
            }
        }
        process.destroy();
        running=false;
        outLog.add("thread end----"+process.exitValue());
    }

    public void stop() {
        running=false;
    }

    public boolean isAlive() {
        return running;
    }
}
