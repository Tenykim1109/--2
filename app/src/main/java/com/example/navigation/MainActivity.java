package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.speech.tts.TextToSpeech;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.util.Log;


public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;//음성출력 객체



    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //selectDoc();//search the DB info

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
    }


    public void Click(View v){// 비콘 수신 기능 개발 전 다음 페이지로 넘어가기 위한 버튼 (Activity 이동)
        Intent intent = new Intent(MainActivity.this,select_destination.class);
        intent.putExtra("beacon_name","beacon_name1");//beacon_name 넘겨주기 (string name, UUID)

        //비콘 수신 기능 구현 시 사용
        String speak = "안녕하세요";
        tts.setPitch(1.5f);//tone
        tts.setSpeechRate(1.0f);//speed
        tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null);//speech


        startActivity(intent);//select_destination 페이지로 이동
    }

    /*private void selectDoc(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();//make the firestore instance

        DocumentReference docRef = db.collection("beacon").document("beacon_name1").collection("route").document("first_route");//document reference
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.d("beacon info","documentSnapshot data: "+document.getData());
                    }
                    else    Log.d("beacon info","No such documnet");
                }
                else    Log.d("beacon info","get failed with ", task.getException());
            }
        });



        db.collection("beacon").document("beacon_name1").collection("route")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                Log.d("beacon"," => " + document.getData());//print all path
                                Current_beacon beacon = document.toObject(Current_beacon.class);
                                Log.d("beacon", "dest_name = " + beacon.getDestination_name());
                                //Log.d("beacon", "inter_path = " + Arrays.toString(beacon.getIntermediate_path()));
                            }
                        }
                        else Log.d("beacon","Error getting documnets: " + task.getException());
                    }
                });
    }*/
}
