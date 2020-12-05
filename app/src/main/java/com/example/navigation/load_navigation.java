package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

import java.util.HashMap;
import java.util.Locale;

public class load_navigation extends AppCompatActivity {
    TextToSpeech tts;//음성출력 객체


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_navigation);

        Intent intent = getIntent();//인식한 비콘에 대한 route 정보담은 객체 받기
        Current_beacon beacon = (Current_beacon) intent.getSerializableExtra("beacon_obj");

        Log.d("beacon_navi", "dest_name = " + beacon.getDest_name());
        Log.d("beacon_navi", "inter_path = " + beacon.getInter_path());
        Log.d("beacon_navi", "navigation = " + beacon.getNavigation());

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });


//        String speak = beacon.getVoice("start");

        String speak = "안녕하세요.";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //안드로이드 빌드버전이 롤리팝(API 21) 이상일 때
            ttsGreater21(speak);
        } else {
            ttsUnder20(speak);
        }

 /*       tts.setPitch(1.5f);//tone
        tts.setSpeechRate(1.0f);//speed
        tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null,null);//speech*/

        /*Intent intent = getIntent();//receive the beacon_name(UUID)
        String doc_route = intent.getStringExtra("doc_route");//통신 중인 비콘 UUID 변수

        String speak = beacon.getVoice("start");
        tts.setPitch(1.5f);//tone
        tts.setSpeechRate(1.0f);//speed
        tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null,null);//speech

        /*String doc_route = intent.getStringExtra("doc_route");//통신 중인 비콘 UUID 변수
        FirebaseFirestore db = FirebaseFirestore.getInstance();//make the firestore instance

        DocumentReference docRef = db.collection("Beacon").document("UUID1").collection("Route").document(doc_route);//document reference
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
        });*/ //intent로 currnet_beacon 객체 못받아올 경우 db 접근 코드
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(tts!=null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}
