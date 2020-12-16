package com.example.navigation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class arrival_info extends AppCompatActivity {

    ImageView dest_imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival_info);

        Intent intent = getIntent();//인식한 비콘에 대한 route 정보담은 객체 받기
        String dest_name = intent.getStringExtra("destination");
        dest_imageView = (ImageView)findViewById(R.id.destnation_image);

        if(dest_name.equals("승강장")) {
            dest_imageView.setImageResource(R.drawable.platform);
        }

        TextView tv = (TextView) findViewById(R.id.destText);//목적지 글자 출력
        tv.setText(dest_name);

    }

    public void Click(View v){//경로재탐색 버튼
        Intent intent = new Intent(arrival_info.this, MainActivity.class);//클릭시 MainActivity로 이동
        startActivity(intent);
    }

}
