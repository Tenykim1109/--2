package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class select_destination extends AppCompatActivity {
    ArrayList<String> destination = new ArrayList<String>();
    int count =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_destination);
        Intent intent = getIntent();//receive the beacon_name(UUID)
        String beacon_name = intent.getStringExtra("beacon_name");//save the received the beacon_name(UUID)
        if( beacon_name != null){
            ListView listView = (ListView) findViewById(R.id.listView);//listview instance
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, destination);
            listView.setAdapter(adapter);

            //update desination lists
            FirebaseFirestore db = FirebaseFirestore.getInstance();//get the firestore access
            db.collection("beacon").document(beacon_name).collection("route")//search the db info
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot document : task.getResult()){
                                    Log.d("beacon"," => " + document.getData());//print all possible path
                                    Current_beacon beacon = document.toObject(Current_beacon.class);//db datamodel
                                    destination.add(beacon.getDestination_name());//db info add the arraylist
                                    //Log.d("beacon", "dest_name = " + beacon.getDestination_name());
                                    Log.d("beacon", "array = " + destination.get(count));//print log about the db info
                                    //Log.d("beacon", "inter_path = " + Arrays.toString(beacon.getIntermediate_path()));
                                    count++;//total number of path
                                    adapter.notifyDataSetChanged();//update the adapter
                                }
                            }
                            else Log.d("beacon","Error getting documnets: " + task.getException());
                        }
                    });

            listView.setOnItemClickListener(//Click the des array_event
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int i, long id) {

                            String item = String.valueOf(parent.getItemAtPosition(i));//클릭한 위치의 des values
                            Toast.makeText(select_destination.this, item, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(select_destination.this,load_navigation.class);//목적지 선택 시 arrival_info로 이동
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
