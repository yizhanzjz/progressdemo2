package com.example.yizhan.a2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DotsProgress dotsProgres = findViewById(R.id.dotsProgres);
        dotsProgres.setText("正在加载中");
        dotsProgres.startProgress();
    }
}
