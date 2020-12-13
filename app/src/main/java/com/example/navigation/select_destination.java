package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;

import android.graphics.Color;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class select_destination extends AppCompatActivity {
    ArrayList<Current_beacon> beacon = new ArrayList<Current_beacon>();
    ArrayList<String> destination = new ArrayList<String>();//목적지 목록 배열_listview adapter 연결용
    int count = 0;

    TextToSpeech tts;//음성출력 객체
    SpeechRecognizer mRecognizer;
    ToneGenerator mGenerator;
    Intent speachIntent;
    final int PERMISSION = 1;
    String dest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_destination);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);//음성인식 버튼
        fab.setOnClickListener(new FABClickListener());

        Intent intent = getIntent();//receive the beacon_name(UUID)
        final String beacon_uuid = intent.getStringExtra("beacon_uuid");//통신 중인 비콘 UUID 변수
        dest = "";

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        mGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        speachIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speachIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        speachIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        if( beacon_uuid != null){
            ListView listView = (ListView) findViewById(R.id.listView);//listview instance
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, destination){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    tv.setTextColor(Color.WHITE);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(40);
                    tv.setPadding(0,40,0,40);
                    return view;
                }
            };
            listView.setAdapter(adapter);

            //update desination lists
            FirebaseFirestore db = FirebaseFirestore.getInstance();//get the firestore access
            db.collection("Beacon").document(beacon_uuid).collection("Route")//search the db info
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot document : task.getResult()){
                                    //Log.d("beacon"," => " + document.getData());//print all possible path
                                    beacon.add(document.toObject(Current_beacon.class));//db datamodel
                                    destination.add(beacon.get(count).getDest_name());//db info add the arraylist
                                    Log.d("beacon", "dest_name = " + beacon.get(count).getDest_name());
                                    Log.d("beacon", "inter_path = " + beacon.get(count).getInter_path());
                                    Log.d("beacon", "navigation = " + beacon.get(count).getNavigation());
                                    count++;//total number of path
                                    adapter.notifyDataSetChanged();//update the adapter
                                }
                            } 
                            else {
                                Log.d("beacon","Error getting documnets: " + task.getException());
                                //String speak = beacon[0].getVoice("UUID2");
                                //tts.setPitch(1.5f);//tone
                                //tts.setSpeechRate(1.0f);//speed
                                //tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null);//speech
                            }
                        }
                    });


            listView.setOnItemClickListener(//Click the des array_event
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                            //String item = String.valueOf(parent.getItemAtPosition(i));//클릭한 위치의 des values
                            String destination = String.valueOf(parent.getItemAtPosition(i));
                            int obj = 1;
                            for(int j=0; j<beacon.size();j++){
                                if (beacon.get(j).getDest_name() == destination)
                                    obj = j;
                            }

                            Intent intent = new Intent(select_destination.this,load_navigation.class);//목적지 선택 시 arrival_info로 이동
                            //intent.putExtra("doc_route",destination);
                            intent.putExtra("beacon_obj",beacon.get(obj));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //안드로이드 빌드버전이 롤리팝(API 21) 이상일 때
                                ttsGreater21(destination+"으로 안내합니다.");
                            } else {
                                ttsUnder20(destination+"으로 안내합니다.");
                            }
                            startActivity(intent);
                        }
                    }

            );

        }

    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) { //안드로이드 버전 20 이하
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) { //안드로이드 버전 21 이상
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) { //에러 상황
            String message;

            switch(error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트워크 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "Recognizer가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 에러";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간 초과";
                    break;
                default:
                    message = "알 수 없는 오류";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) { //음성인식 후 결과처리
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            int obj = 1;
            for (int i = 0; i < matches.size(); i++) {
                dest+=matches.get(i);
            }

            for(int j=0; j<beacon.size();j++){
                if (beacon.get(j).getDest_name() == dest)
                    obj = j;
            }

            Intent intent = new Intent(select_destination.this,load_navigation.class);//목적지 선택 시 arrival_info로 이동
            //intent.putExtra("doc_route",destination);
            intent.putExtra("beacon_obj",beacon.get(obj));

            Toast.makeText(getApplicationContext(), dest+"(으)로 안내합니다.", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //안드로이드 빌드버전이 롤리팝(API 21) 이상일 때
                ttsGreater21(dest+"(으)로 안내합니다.");
            } else {
                ttsUnder20(dest+"(으)로 안내합니다.");
            }
            startActivity(intent);
            dest="";
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null) {
            tts.stop();
            tts.shutdown();
        }
    }


    class FABClickListener implements  View.OnClickListener{//음성인식 floating button
        @Override
        public void onClick(View view) {//floating button 클릭 시 음성인식 코드
            String text = "목적지를 말씀해주세요.";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //안드로이드 빌드버전이 롤리팝(API 21) 이상일 때
                ttsGreater21(text);
            } else {
                ttsUnder20(text);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //비프음 후 음성인식
            mGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(speachIntent);
        }
    }
}
