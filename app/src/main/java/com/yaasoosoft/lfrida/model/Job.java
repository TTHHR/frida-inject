package com.yaasoosoft.lfrida.model;

import com.google.gson.Gson;

public class Job {
    private boolean logToFile;
    private String name;
    private String jsPath;
    private String appName;
    private String appPackageName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsPath() {
        return jsPath;
    }

    public void setJsPath(String jsPath) {
        this.jsPath = jsPath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    @Override
    public String toString()
    {
        return new Gson().toJson(this);
    }
    public static Job getJob(String json)
    {
       return new Gson().fromJson(json,Job.class);
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public boolean isLogToFile() {
        return logToFile;
    }

    public void setLogToFile(boolean logToFile) {
        this.logToFile = logToFile;
    }
}
