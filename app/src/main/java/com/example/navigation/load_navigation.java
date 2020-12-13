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


import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

        //버튼 클릭시 objdetection앱으로 넘어감
        FloatingActionButton detec_btn;

        detec_btn=findViewById( R.id.toDetector);
        detec_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=getPackageManager().getLaunchIntentForPackage("com.toure.objectdetection");
                startActivity(i);
            }
        });

        Intent intent = getIntent(); //인식한 비콘에 대한 route 정보담은 객체 받기
        imageView = (ImageView)findViewById(R.id.imageView);

        beacon = (Current_beacon) intent.getSerializableExtra("beacon_obj"); //select_destination에서 수신한 비콘에 대한 정보 받기
        beaconManager = BeaconManager.getInstanceForApplication(this);

        TextView tv = (TextView) findViewById(R.id.destText);
        tv.setText(beacon.getDest_name());

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
            intent.putExtra("destination",beacon.getDest_name());
            startActivity(intent); //arrival_info 화면으로 이동
        } else {
            imageView.setImageResource(R.drawable.south);
        }
    }

    public void Click(View v){//경로 안내 중단 버튼
        Intent intent = new Intent(load_navigation.this,MainActivity.class);//클릭시 비콘 탐색화면으로 이동
        startActivity(intent);
    }
}
