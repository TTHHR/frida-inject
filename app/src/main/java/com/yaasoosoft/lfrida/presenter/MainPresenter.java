package com.yaasoosoft.lfrida.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.yaasoosoft.lfrida.MainInterface;
import com.yaasoosoft.lfrida.model.Job;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainPresenter {
    private String TAG=MainPresenter.class.getSimpleName();
    SharedPreferences myShare;
    Set<String> jobList=null;
    private  String homePath;
    private  String cachePath;
    private String injectPath ="";
    private boolean running=false;

    private MainPresenter(){};
    private List<JobThread> jobProcess;
    private MainInterface mainInterface;
    private Thread logThread=null;
    public MainPresenter(MainInterface mainInterface)
    {
        jobProcess=new ArrayList<>();
        this.mainInterface=mainInterface;
        injectPath=((Context)mainInterface).getApplicationInfo().nativeLibraryDir+"/libinject.so";
        homePath=((Context)mainInterface).getFilesDir().getPath();
        cachePath=((Context)mainInterface).getCacheDir().getPath();
        File file=new File(homePath);
        if(!file.exists())
            file.mkdir();
        file=new File(cachePath);
        if(!file.exists())
            file.mkdir();
    }

    public String[] getJobNames() {
        if(jobList==null) {
            myShare = ((Context) mainInterface).getSharedPreferences("setting", Context.MODE_PRIVATE);
            jobList = myShare.getStringSet("jobList", null);
            if (jobList == null)
                jobList = new HashSet<>();
        }
        String[] data=new String[jobList.size()+1];
        int index=0;
        data[index]="新增";
        for (String name:jobList) {
            index++;
            data[index]=name;
        }
        return data;
    }
    public void removeJobByName(String name)
    {
        if(jobList==null)
            return;
    }
    public void save()
    {
        Log.e(TAG,"save "+jobList);
        if(jobList!=null&&myShare!=null)
        {
            jobList.remove("新增");
            myShare.edit().putStringSet("jobList",jobList).apply();
        }
    }

    public void addJob(Job job) {
        if (jobList == null)
        {
            mainInterface.showToast("程序异常");
            return;
        }
        if(job.getName()==null||job.getName().isEmpty())
        {
            mainInterface.showToast("任务名称不能为空");
            return;
        }
        if(job.getAppPackageName()==null||job.getAppPackageName().isEmpty())
        {
            mainInterface.showToast("包名不能为空");
            return;
        }
        if(job.getJsPath()==null||job.getJsPath().isEmpty())
        {
            mainInterface.showToast("JS文件不能为空");
            return;
        }
        if(jobList.contains(job.getName()))
        {
            mainInterface.showToast("该任务已存在，建议改名");
            return;
        }
        jobList.add(job.getName());
        myShare.edit().putString(job.getName(),job.toString()).apply();
        mainInterface.showToast("添加成功");
        mainInterface.refreshJobList();
        save();
    }

    public Job getJobByName(String name) {
        if (jobList == null)
        {
            mainInterface.showToast("程序异常");
            return null;
        }
        if(!jobList.contains(name))
        {
            mainInterface.showToast("该任务不存在");
            return null;
        }
        String json= myShare.getString(name,"");
        return Job.getJob(json);
    }

    public void updateJob(Job job) {
        if (jobList == null)
        {
            mainInterface.showToast("程序异常");
            return ;
        }
        if(!jobList.contains(job.getName()))
        {
            addJob(job);
        }
        if(job.getName()==null||job.getName().isEmpty())
        {
            mainInterface.showToast("任务名称不能为空");
            return;
        }
        if(job.getAppPackageName()==null||job.getAppPackageName().isEmpty())
        {
            mainInterface.showToast("包名不能为空");
            return;
        }
        if(job.getJsPath()==null||job.getJsPath().isEmpty())
        {
            mainInterface.showToast("JS文件不能为空");
            return;
        }
        myShare.edit().putString(job.getName(),job.toString()).apply();
        mainInterface.showToast("更新成功");
        mainInterface.refreshJobList();
        save();
    }

    public void stopJob(String jobName) {
        JobThread jobThread=null;
        for (int i = 0; i < jobProcess.size(); i++) {
            if(jobName.equals(jobProcess.get(i).getName()))
            {
                jobThread=jobProcess.get(i);
            }
        }
        if(jobThread!=null)
        {
            jobThread.stop();
            mainInterface.addLog("停止"+jobName+"完成");
        }
        else
        {
            mainInterface.showToast(jobName+" 没有在运行");
        }
    }

    public void startJob(String jobName) {
        if(logThread==null||!running)
        {
            logThread=new Thread(new LogPrint());
            logThread.start();
        }
        boolean found=false;
        for (String job:jobList
             ) {
            if(job.equals(jobName))
            {
                 found = true;
                 break;
            }
        }
        if(!found)
        {
            mainInterface.showToast("程序异常");
            return ;
        }
        File adbFile=new File(injectPath);
        if(!adbFile.exists())
        {
            Log.e(TAG, injectPath +" not exist");
            mainInterface.addLog("初始化失败 "+injectPath +" not exist");
            return;
        }
        JobThread jobThread=null;
        for (int i = 0; i < jobProcess.size(); i++) {
            if(jobName.equals(jobProcess.get(i).getName()))
            {
                jobThread=jobProcess.get(i);
            }
        }
        if(jobThread!=null&&jobThread.isAlive())
        {
            mainInterface.showToast("已经在运行了");
            return;
        }
        else
        {
            runJob(jobName);
            mainInterface.addLog(jobName+" 启动");

        }
    }
    private void runJob(String jobName)
    {

        Job job=Job.getJob(myShare.getString(jobName,""));

            JobThread rs=new JobThread(job,injectPath,homePath,cachePath);
            Thread thread=new Thread(rs);
            thread.setName(jobName);
            thread.start();
            jobProcess.add(rs);
    }
    private class LogPrint implements Runnable
    {

        @Override
        public void run() {
            running=true;
            String line="";
            Map<String,FileWriter> fws=new HashMap<>();
            while (running&&!Thread.interrupted())
            {
                try {
                    Thread.sleep(10);
                    for (int i = 0; i < jobProcess.size(); i++) {
                        line=jobProcess.get(i).getOutput();
                        if(line!=null) {
                            if(!jobProcess.get(i).job.isLogToFile())
                            mainInterface.addLog(line);
                            else
                            {
                                if(fws.containsKey(jobProcess.get(i).getName()))
                                {
                                    FileWriter fw=fws.get(jobProcess.get(i).getName());
                                    if(fw==null)
                                    {
                                        fw=new FileWriter()
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            running=false;
        }
    }
    public void stopAll()
    {
        for (int i = 0; i < jobProcess.size(); i++) {
            jobProcess.get(i).stop();
        }
    }
}
