package com.example.myapplication;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.IBinder;

public class JobSchedulerService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
