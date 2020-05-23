package com.example.myapplication;
//importing time management packages
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

//import android views capability
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

//importing json parsing classes
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//importing https request sender classes
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//importing notifications class
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class MainActivity extends AppCompatActivity {

    TextView txtString;
    public String url= "https://codeforces.com/api/contest.list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ComponentName serviceName = new ComponentName(this, JobSchedulerService.class);
        JobInfo jobInfo = new JobInfo.Builder(11, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build();
        txtString= (TextView)findViewById(R.id.txtString);

        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendNotification(View view){
        //Create an intent to get into activity from notification
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get the instance of the notification manager
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Contests upcoming")
                .setContentText("Prepare for the upcoming round!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[] {0, 1000, 1000, 1000})
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        //Get the instance of the notification manager service

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }

    void run() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse = response.body().string();
                final String answer = myResponse;
                try{
                    final JSONObject obj = new JSONObject(answer);
                    final JSONArray contests = obj.getJSONArray("result");
                    final int n = contests.length();
                    for(int i=0; i<n; i++){
                        final JSONObject contest = contests.getJSONObject(i);
                        final String phase = contest.getString("phase");
                        final String name = contest.getString("name").concat("\n");
                        int seconds = contest.getInt("startTimeSeconds");
                        ZoneOffset zoneOffSet= ZoneOffset.of("+05:00");
                        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(seconds, 0, zoneOffSet);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
                        final String time = dateTime.format(formatter).concat("\n\n");
                        if(phase.equals("BEFORE")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtString.append(name);
                                    txtString.append(time);
                                }
                            });
                        }
                    }
                }
                catch (JSONException e){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtString.append("Server error");
                        }
                    });
                }
            }
        });
    }

}
