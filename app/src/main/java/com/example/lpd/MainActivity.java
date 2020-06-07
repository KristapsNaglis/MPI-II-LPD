package com.example.lpd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private int EXTERNAL_READ_WRITE_PERMISSION_CODE = 111;

    private Chronometer chronometerWorkTime;
    private Chronometer chronometerBreakTime;
    private long workTimeOffset;
    private long breakTimeOffset;
    private boolean isWorking;
    private TextView dateTime;
    private TextView dateFull;
    private String workTimeToday;
    private String breakTimeToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions(getBaseContext());

        chronometerWorkTime = findViewById(R.id.chronometerWorkTime);
        chronometerBreakTime = findViewById(R.id.chronometerBreakTime);
        dateTime = findViewById(R.id.dayDate);
        dateFull = findViewById(R.id.fullDate);

        chronometerWorkTime.setFormat("Work Time: %s");
        chronometerBreakTime.setFormat("Break Time: %s");

        chronometerWorkTime.setBase(SystemClock.elapsedRealtime());
        chronometerBreakTime.setBase(SystemClock.elapsedRealtime());

        setUpDates();
    }

    public void setUpDates(){
        Date date = new Date();

        // Day name configuration and assigning
        SimpleDateFormat dayName = new SimpleDateFormat("EEEE");
        dateTime.setText(dayName.format(date));

        // Full date configuration and assigning
        SimpleDateFormat fullDate = new SimpleDateFormat("dd MMM yyyy");
        dateFull.setText(fullDate.format(date));
    }

    public void startWorking(View view){
        if (!isWorking) {
            // Work time chronometer calculate
            chronometerWorkTime.setBase(SystemClock.elapsedRealtime() - workTimeOffset);
            chronometerWorkTime.start();

            // Break time chronometer calculate
            chronometerBreakTime.stop();
            breakTimeOffset = SystemClock.elapsedRealtime() - chronometerBreakTime.getBase();

            // Toggle working identifier
            isWorking = true;
        }
    }

    public void takeBreak(View view){
        if (isWorking) {
            // Work time chronometer calculate
            chronometerWorkTime.stop();
            workTimeOffset = SystemClock.elapsedRealtime() - chronometerWorkTime.getBase();

            // Break time chronometer calculate
            chronometerBreakTime.setBase(SystemClock.elapsedRealtime() - breakTimeOffset);
            chronometerBreakTime.start();

            // Toggle working identifier
            isWorking = false;
        }
    }

    public void endWorkday(View view){

        if (isWorking) {
            workTimeOffset = SystemClock.elapsedRealtime() - chronometerWorkTime.getBase();
            chronometerWorkTime.stop();
        } else {
            breakTimeOffset = SystemClock.elapsedRealtime() - chronometerBreakTime.getBase();
            chronometerBreakTime.stop();
        }

        // Assign chronometer times to variables to later write to file
        // workTimeToday = (String) chronometerWorkTime.getBase();
        int elapsedWorkTime = (int)(workTimeOffset);
        int elapsedBreakTime = (int)(breakTimeOffset);

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        workTimeToday = formatter.format(new Date(elapsedWorkTime));
        breakTimeToday = formatter.format(new Date(elapsedBreakTime));

        chronometerWorkTime.setBase(SystemClock.elapsedRealtime());
        chronometerBreakTime.setBase(SystemClock.elapsedRealtime());

        writeToFile(java.text.DateFormat.getDateTimeInstance().format(new Date()));

        workTimeOffset = 0;
        breakTimeOffset = 0;

    }

    private void throwConfirmationAlert(){
        new AlertDialog.Builder(this)
                .setTitle("End Workday")
                .setMessage("Do you really want to end your workday?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //writeToFile(java.text.DateFormat.getDateTimeInstance().format(new Date()), getBaseContext());
                        writeToFile(java.text.DateFormat.getDateTimeInstance().format(new Date()));
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void writeToFile(String title){

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/work_logs");
        dir.mkdirs();
        File file = new File(dir, title + ".txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("Work Time: " + workTimeToday);
            pw.println("Break Time: " + breakTimeToday);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("Kristaps", "File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hamburger_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        // Left as switch not an if because of future expansions
        switch (item.getItemId()) {
            case R.id.worklog:
                Intent intent = new Intent(this, WorkLog.class);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }

    public boolean checkPermissions(Context context){
        if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT < 23) {
                int permissionRequest = PermissionChecker.checkSelfPermission(context, READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE);
                return permissionRequest == PermissionChecker.PERMISSION_GRANTED;
            } else {
                String[] permissionRequest = {READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissionRequest, EXTERNAL_READ_WRITE_PERMISSION_CODE);
                return true;
            }

        }
    }
}
