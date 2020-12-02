package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class load_navigation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_navigation);

        /*Intent intent = getIntent();//receive the beacon_name(UUID)
        String doc_route = intent.getStringExtra("doc_route");//통신 중인 비콘 UUID 변수


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
        });*/
    }
}
