package com.example.lpd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class WorkLog extends AppCompatActivity {

    private ListView workLogList;
    private String workLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_log);

        workLogList = findViewById(R.id.workLogList);

        // Read all files and assign them to list
        readFiles(workLogList);

        // Get selected item from list and read it
        workLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(WorkLog.this, parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                workLog = readFromFile(parent.getItemAtPosition(position));

                Intent intent = new Intent(getBaseContext(), WorkLogContents.class);
                String message = workLog;
                intent.putExtra("work_log", message);
                startActivity(intent);
            }
        });
    }

    private void readFiles(ListView listView) {

        final String root = Environment.getExternalStorageDirectory().toString();
        final File dir = new File(root + "/work_logs");

        try {
            //listView = audioFileListView;
            List<String> fileListArray = new ArrayList<String>();
            File[] files = dir.listFiles();
            for (File file : files) {
                Log.d("Kristapsfiles", "FileName:" + file.getName());
                fileListArray.add(file.getName());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getBaseContext(),
                    android.R.layout.simple_list_item_1,
                    fileListArray);
            listView.setAdapter(arrayAdapter);
        } catch (Exception e) {
            Log.d("INFO", "No files are in folder");
        }
    }

    private String readFromFile(Object item) {

        String ret = "";
        final String root = Environment.getExternalStorageDirectory().toString();
        final File dir = new File(root + "/work_logs");
        File file = new File(dir, item.toString());

        try {
            InputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                Log.e("Kristaps", ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
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
            case R.id.main:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }
}
