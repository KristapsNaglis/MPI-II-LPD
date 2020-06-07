package com.example.lpd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class WorkLogContents extends AppCompatActivity {

    private TextView workLlogTextView;
    private String worklogContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_log_contents);

        workLlogTextView = findViewById(R.id.worklogContent);

        worklogContent = getIntent().getStringExtra("work_log");

        workLlogTextView.setText(worklogContent);

    }
}
