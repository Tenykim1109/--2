package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class select_destination extends AppCompatActivity {
    Current_beacon [] beacon = new Current_beacon[20];//목적지 목록 저장 객체 배열
    ArrayList<String> destination = new ArrayList<String>();//목적지 목록 배열_listview adapter 연결용
    int count =0;

    TextToSpeech tts;//음성출력 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_destination);
        Intent intent = getIntent();//receive the beacon_name(UUID)
        final String beacon_uuid = intent.getStringExtra("beacon_uuid");//통신 중인 비콘 UUID 변수

        tts = new TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        if( beacon_uuid != null){
            ListView listView = (ListView) findViewById(R.id.listView);//listview instance
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, destination);
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
                                    beacon[count] = document.toObject(Current_beacon.class);//db datamodel
                                    destination.add(beacon[count].getDest_name());//db info add the arraylist
                                    Log.d("beacon", "dest_name = " + beacon[count].getDest_name());
                                    Log.d("beacon", "inter_path = " + beacon[count].getInter_path());
                                    Log.d("beacon", "navigation = " + beacon[count].getNavigation());
                                    count++;//total number of path
                                    adapter.notifyDataSetChanged();//update the adapter
                                }
                            }
                            else Log.d("beacon","Error getting documnets: " + task.getException());

                            String speak = beacon[0].getVoice("UUID2");
                            tts.setPitch(1.5f);//tone
                            tts.setSpeechRate(1.0f);//speed
                            tts.speak(speak,TextToSpeech.QUEUE_FLUSH,null);//speech
                        }
                    });


            listView.setOnItemClickListener(//Click the des array_event
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                            //String item = String.valueOf(parent.getItemAtPosition(i));//클릭한 위치의 des values
                            //Toast.makeText(select_destination.this, item, Toast.LENGTH_SHORT).show();
                            String destination = String.valueOf(parent.getItemAtPosition(i));
                            Intent intent = new Intent(select_destination.this,load_navigation.class);//목적지 선택 시 arrival_info로 이동
                            intent.putExtra("doc_route",destination);
                            //intent.putExtra("beacon_uuid",beacon_uuid);
                            startActivity(intent);
                        }
                    }

            );
        }


    }

    /*private void list_dest( String beacon_name ){
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



        db.collection("beacon").document(beacon_name).collection("route")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                Log.d("beacon"," => " + document.getData());//print all path
                                Current_beacon beacon = document.toObject(Current_beacon.class);
                                destination.add(beacon.getDestination_name());
                                //Log.d("beacon", "dest_name = " + beacon.getDestination_name());
                                Log.d("beacon", "array = " + destination.get(count));
                                //Log.d("beacon", "inter_path = " + Arrays.toString(beacon.getIntermediate_path()));
                                count++;
                                //adapter.notifyDataSetChanged();
                            }
                        }
                        else Log.d("beacon","Error getting documnets: " + task.getException());
                    }
                });
    }*/
}
