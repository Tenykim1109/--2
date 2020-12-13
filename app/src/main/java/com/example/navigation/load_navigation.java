package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Locale;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class load_navigation extends AppCompatActivity implements BeaconConsumer{
    TextToSpeech tts;
    private BeaconManager beaconManager;
    private List<Beacon> beaconList;
    Current_beacon beacon;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_navigation);

        Intent intent = getIntent(); //인식한 비콘에 대한 route 정보담은 객체 받기
        imageView = (ImageView)findViewById(R.id.imageView);

        beacon = (Current_beacon) intent.getSerializableExtra("beacon_obj"); //select_destination에서 수신한 비콘에 대한 정보 받기
        beaconManager = BeaconManager.getInstanceForApplication(this);

        TextView tv = (TextView) findViewById(R.id.destText);
        tv.setText(beacon.getDest_name());

        ///////////////////////////
        //ArrayList<String> inter_path = new ArrayList<String>();
        //inter_path.addAll(beacon.getInter_path());//beacon이 가진 중간경로 arraylist 복사 -> 이용해서 새로운 beacon 수신 시마다 비교해주면 됨
        //수신된 beacon의 inter_path 내 minor값을 이용하연 getvoice로 음성안내 가능
        //beacon.getVoide("inter_path[n]"); n자리에 각 중간 경로 순서 입력

        //목적지 도착 시 도착 안내 페이지로 이동 코드_arrival_info
        //Intent next_intent = new Intent(load_navigation.this,arrival_info.class);
        //next_intent.putExtra("destination",beacon.getDest_name());
        //startActivity(next_intent);


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

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //beacon i4 고유 레이아웃, 비콘마다 레이아웃값 다 다름.
        beaconManager.bind(this); //비콘 탐지 시


//        String speak = beacon.getVoice("start");

        /*String speak = "안녕하세요.";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //안드로이드 빌드버전이 롤리팝(API 21) 이상일 때
            ttsGreater21(speak);
        } else {
            ttsUnder20(speak);
        }*/

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

        try {
            beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.unbind(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.setSpeechRate(0.8f);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.setSpeechRate(0.85f);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) { //비콘이 감지되었을때 실행되는 함수
                if (beacons.size() > 0) {
                    if(beacons.iterator().next().getDistance()>=1.0 && beacons.iterator().next().getDistance()<=2.5) { //비콘 인식 거리는 1미터에서 1.5미터
                        Log.d("beacon_uuid", beacons.iterator().next().getId3().toString());
                        load_guide(beacons.iterator().next().getId3().toString(), beacon);
                    }
//                    beaconList.clear();
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    private void load_guide(String UUID, Current_beacon beacon) {
        Intent intent = new Intent(load_navigation.this, arrival_info.class);
        Log.d("beacon_minorID", UUID);
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
        /*for (Map.Entry<String, String> entry : beacon.getNavigation().entrySet()) {
            Log.d("beacon_key", entry.getKey());
            if (entry.getKey() == UUID) {
                Log.d("beacon_path", "text: " + beacon.getNavigation().get(UUID));
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(beacon.getNavigation().get(UUID));
                }
                else {
                    ttsUnder20(beacon.getNavigation().get(UUID));
                }
            }
        }*/

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(beacon.getNavigation().get(UUID));
        } else {
            ttsUnder20(beacon.getNavigation().get(UUID));
        }

        if (beacon.getNavigation().get(UUID).contains("전진")) {
            imageView.setImageResource(R.drawable.north);
        } else if (beacon.getNavigation().get(UUID).contains("좌회전") || beacon.getNavigation().get(UUID).contains("좌측")) {
            imageView.setImageResource(R.drawable.east);
        } else if (beacon.getNavigation().get(UUID).contains("우회전") || beacon.getNavigation().get(UUID).contains("우측")) {
            imageView.setImageResource(R.drawable.west);
        } else if(beacon.getNavigation().get(UUID).contains("목적지")) {
            startActivity(intent); //arrival_info 화면으로 이동
        } else {
            imageView.setImageResource(R.drawable.south);
        }
    }
}
