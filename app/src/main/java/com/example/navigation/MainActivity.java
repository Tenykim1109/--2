package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;


public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    private static final String TAG = null;
    TextToSpeech tts;//음성출력 객체
    private BeaconManager beaconManager; //비콘 인식 객체
    private List<Beacon> beaconList = new ArrayList<>(); //비콘 목록을 담아둘 리스트
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2; //비콘 감지를 위해 위치 권한 필요.

//    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //selectDoc();//search the DB info

        beaconManager = BeaconManager.getInstanceForApplication(this); //비콘 관리자
//        compare_beacon("UUID1");//통신 성공한 비콘과 DB 내 비콘 정보 비교

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        /*beaconManager의 레이아웃 설정*/
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //beacon i4 고유 레이아웃, 비콘마다 레이아웃값 다 다름.
        beaconManager.bind(this); //비콘 탐지 시

        //beacon 을 활용하려면 블루투스 권한획득(Andoird M버전 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_BACKGROUND_LOCATION); //권한 요청창 띄우기
                            }

                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {

                            }
                        });
                        builder.show();
                    }

                }
            } else {
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    PERMISSION_REQUEST_FINE_LOCATION);
                        }

                    });
                    builder.show();
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Toast.makeText(getApplicationContext(), "finished!", Toast.LENGTH_SHORT).show();
        try {
            beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.unbind(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        /*beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });*/

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) { //비콘이 감지되었을때 실행되는 함수
                if (beacons.size() > 0) {
//                    compare_beacon(beacons.iterator().next().getId3().toString());
                    if(beacons.iterator().next().getDistance()>=0.0 && beacons.iterator().next().getDistance()<=1.5) {
                        compare_beacon(beacons.iterator().next().getId3().toString());
                    }
                    beaconList.clear();
 /*                   for (Beacon beacon : beacons) {
//                        beacon.getDistance() >=1.0 &&
                        if (beacon.getDistance() >=1.0 && beacon.getDistance() <= 1.5) {
                            beaconList.add(beacon); //리스트에 비콘 목록 추가
                            compare_beacon(beacon.getId3().toString());//통신 성공한 비콘과 DB 내 비콘 정보 비교
                        }
                    }*/
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    public void Click(View v){// 비콘 수신 기능 개발 전 다음 페이지로 넘어가기 위한 버튼 (Activity 이동)
        //비콘 수신 기능 구현 시 사용
        Intent intent = new Intent(getApplicationContext(), select_destination.class);
        String speak = "안녕하세요";
        tts.setPitch(1.5f);//tone
        tts.setSpeechRate(1.0f);//speed
        tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null, null);//speech

        handler.sendEmptyMessage(0); //1초마다 비콘 정보 갱신

        if (beaconList.size()>0) { //비콘이 감지되었을 경우 버튼 누르면 다음 페이지 넘어감.
            Toast.makeText(getApplicationContext(), "비콘이 감지되었습니다.", Toast.LENGTH_SHORT).show();
//            startActivity(intent); //select_destination 페이지로 이동
        } else {
            Toast.makeText(getApplicationContext(), "비콘이 감지되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            /*findBeacon.setText("");

            //비콘 id, 거리정보를 textview에 출력
            for (Beacon beacon : beaconList) {
                findBeacon.append("ID : " + beacon.getId3() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
            }*/

            //자기 자신을 1초마다 호출
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    private void compare_beacon(final String UUID){//통신 비콘과 DB 비콘 비교 메소드
        final boolean[] res = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Beacon")
                .whereEqualTo("UUID", UUID)//UUID1대신 실제 통신한 beacon UUID 입력하여 DB UUID 목록과 비교
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                //비교 완료한 비콘에 대한 목적지 안내 페이지로 이동 (+해당 비콘의 UUID정보와 함께)
                                Intent intent = new Intent(MainActivity.this,select_destination.class);
                                //intent.putExtra("beacon_uuid",uuid.getUUID());//beacon_name 넘겨주기 (string name, UUID)
                                intent.putExtra("beacon_uuid",document.getId());//beacon_name 넘겨주기 (string name, UUID)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                 startActivity(intent);//select_destination 페이지로 이동
                                finish();
                            }
                        }
                    }
                });
    }
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