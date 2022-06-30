package com.yaasoosoft.lfrida;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.yaasoosoft.lfrida.model.Job;
import com.yaasoosoft.lfrida.presenter.MainPresenter;

public class MainActivity extends AppCompatActivity implements MainInterface {
    EditText logText;

    ListView jobList;
    ListAdapter jobsAdapter;
    MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainPresenter = new MainPresenter(this);
        initView();

    }

    void initView() {
        logText = findViewById(R.id.logView);
        jobList = findViewById(R.id.jobList);
        refreshJobList();
        jobList.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                showAddJobDialog();
            } else {
                showJobDetailDialog(mainPresenter.getJobNames()[position]);
            }
        });
        jobList.setOnItemLongClickListener((parent, view, position, id) ->
        {
            if(position<=0)
                return false;
            String jobName=mainPresenter.getJobNames()[position];
            new AlertDialog.Builder(this)
                    .setTitle(jobName)
                    .setMessage("您要做什么")
                    .setPositiveButton("运行", (dialog, which) -> {
                        mainPresenter.startJob(jobName);
                    })
                    .setNegativeButton("停止",(dialog,which)->{
                        mainPresenter.stopJob(jobName);
                    })

                    .show();
            return true;
        });
    }

    private View configLayout = null;
    private EditText nameText, jsPathText, packageNameText;
    private CheckBox logToFile;

    private void showJobDetailDialog(String name) {
        Job job=mainPresenter.getJobByName(name);
        if(job==null)
            return;
        if (configLayout == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            configLayout = inflater.inflate(R.layout.config_layout, null);
            nameText = configLayout.findViewById(R.id.nameText);
            jsPathText = configLayout.findViewById(R.id.jsPathText);
            packageNameText = configLayout.findViewById(R.id.packageNameText);
            logToFile=configLayout.findViewById(R.id.logToFile);
        }
        nameText.setText(job.getName());
        jsPathText.setText(job.getJsPath());
        packageNameText.setText(job.getAppPackageName());
        logToFile.setChecked(job.isLogToFile());
        new AlertDialog.Builder(this)
                .setTitle("Config")
                .setView(configLayout)
                .setPositiveButton("update", (dialog, which) -> {
                    job.setName(nameText.getText().toString());
                    job.setAppPackageName(packageNameText.getText().toString());
                    job.setJsPath(jsPathText.getText().toString());
                    job.setLogToFile(logToFile.isChecked());
                    mainPresenter.updateJob(job);
                })
                .setOnDismissListener(dialogInterface -> {
                    ((ViewGroup) configLayout.getParent()).removeView(configLayout);
                    dialogInterface.dismiss();
                })

                .show();
    }

    private void showAddJobDialog() {
        Job job = new Job();
        if (configLayout == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            configLayout = inflater.inflate(R.layout.config_layout, null);
            nameText = configLayout.findViewById(R.id.nameText);
            jsPathText = configLayout.findViewById(R.id.jsPathText);
            packageNameText = configLayout.findViewById(R.id.packageNameText);
            logToFile=configLayout.findViewById(R.id.logToFile);
        }
        new AlertDialog.Builder(this)
                .setTitle("Config")
                .setView(configLayout)
                .setPositiveButton("done", (dialog, which) -> {
                    job.setName(nameText.getText().toString());
                    job.setAppPackageName(packageNameText.getText().toString());
                    job.setJsPath(jsPathText.getText().toString());
                    job.setLogToFile(logToFile.isChecked());
                    mainPresenter.addJob(job);
                })
                .setOnDismissListener(dialogInterface -> {
                    ((ViewGroup) configLayout.getParent()).removeView(configLayout);
                    dialogInterface.dismiss();
                })

                .show();

    }


    @Override
    public void addLog(String text) {
        runOnUiThread(() -> {
            logText.append("\n");
            logText.append(text);
        });
    }

    @Override
    public void showToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void refreshJobList() {
        runOnUiThread(()->{
            jobsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mainPresenter.getJobNames());
            jobList.setAdapter(jobsAdapter);
        });
    }

    @Override
    protected void onDestroy() {
        mainPresenter.save();
        super.onDestroy();
    }
}