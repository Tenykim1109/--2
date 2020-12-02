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

        compare_beacon("44604");//통신 성공한 비콘과 DB 내 비콘 정보 비교

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
        //비콘 수신 기능 구현 시 사용
        String speak = "안녕하세요";
        tts.setPitch(1.5f);//tone
        tts.setSpeechRate(1.0f);//speed
        tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null);//speech
    }

    private void compare_beacon(String UUID){//통신 비콘과 DB 비콘 비교 메소드
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Beacon")
                .whereEqualTo("UUID",UUID)//UUID1대신 실제 통신한 beacon UUID 입력하여 DB UUID 목록과 비교
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                //beacon_uuid uuid = document.toObject(beacon_uuid.class);
                                Log.d("beacon"," => " + document.getId());//print current beacon UUID

                                //비교 완료한 비콘에 대한 목적지 안내 페이지로 이동 (+해당 비콘의 UUID정보와 함께)
                                Intent intent = new Intent(MainActivity.this,select_destination.class);
                                //intent.putExtra("beacon_uuid",uuid.getUUID());//beacon_name 넘겨주기 (string name, UUID)
                                intent.putExtra("beacon_uuid",document.getId());
                                startActivity(intent);//select_destination 페이지로 이동
                            }
                        }
                    }
                });
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
class beacon_uuid{//DB 비콘 정보 클래스_UUID1 document
    private String UUID;

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getUUID() {
        return UUID;
    }
}