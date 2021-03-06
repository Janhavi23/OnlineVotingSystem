package com.example.onlinevoting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class register extends AppCompatActivity {
    EditText name,date_of_birth,phone_no,voters_id;
    TextView login;
    Button register;
    String date="";
    ArrayAdapter<String> myAdapter;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        Spinner gender = (Spinner) findViewById(R.id.gender);
        myAdapter = new ArrayAdapter<String>(register.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(myAdapter);


        name = (EditText) findViewById(R.id.name);
        date_of_birth = (EditText) findViewById(R.id.dob);
        phone_no = (EditText) findViewById(R.id.phoneno);
        voters_id = (EditText) findViewById(R.id.votersId);
        register = (Button) findViewById(R.id.reg);
        login = (TextView) findViewById(R.id.login);
        db = FirebaseFirestore.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),login.class);
                startActivity(intent);
            }
        });
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        register.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        date = day + "/" + month + "/" + year;
                        Calendar today = Calendar.getInstance();
                        int age = today.get(Calendar.YEAR) - year;
                        if (age < 18) {
                            date_of_birth.setError("Your age should be above 18");
                        } else {
                            date_of_birth.setText(date);
                        }
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db=FirebaseFirestore.getInstance();
                CollectionReference collectionReference=db.collection("users");
                if (!validateName() || !validatedob() || !validatphoneno() || !validatvotersid()) {
                    return;
                }
                DocumentReference documentReference=collectionReference.document(phone_no.getText().toString());
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                               Toast.makeText(register.this,"You are already registered. Please login to continue",Toast.LENGTH_LONG).show();
                                Intent intent=new Intent(getApplicationContext(),login.class);
                                startActivity(intent);
                            } else {
                                if (!validateName() || !validatedob() || !validatphoneno() || !validatvotersid()) {
                                    return;
                                }
                                String name_str = name.getText().toString();
                                String dob = date_of_birth.getText().toString();
                                Long phoneno = Long.parseLong(phone_no.getText().toString());
                                String votersId = voters_id.getText().toString();
                                String gend = gender.getSelectedItem().toString();
                                Map<String, Object> user = new HashMap<>();
                                user.put("Name", name_str);
                                user.put("DateOfBirth", dob);
                                user.put("PhoneNumber", phoneno);
                                user.put("VotersId", votersId);
                                user.put("Gender", gend);
                                user.put("Voted",false);
                                db.collection("users").document(String.valueOf(phoneno))
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener() {
                                            @Override
                                            public void onSuccess(Object o) {
                                                Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
                                            }

                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                Intent intent=new Intent(getApplicationContext(),login.class);
                                intent.putExtra("mobile", phone_no.getText().toString());
                                startActivity(intent);
                            }
                        }
                    }
                });

            }
        });
    }

    private Boolean validateName () {
        String name_str = name.getText().toString();
        if(name_str.isEmpty()  ){
            name.setError("It cannot be empty");
            return false;
        }
        else {
            name.setError(null);
            return  true;
        }

    }
    private Boolean validatedob () {
        String dob= date_of_birth.getText().toString();
        if(dob.isEmpty()  ){
            date_of_birth.setError("It cannot be empty");
            return false;
        }
        else {
            date_of_birth.setError(null);
            return  true;
        }

    }
    private Boolean validatphoneno () {
        String phone= phone_no.getText().toString();
        if(phone.isEmpty()){
            phone_no.setError("It cannot be empty");
            return false;
        }
        else if(phone.length()!=10){
            phone_no.setError("It should be 10-digit phone number");
            return false;
        }
        else{
            phone_no.setError(null);
            return  true;
        }

    }
    private Boolean validatvotersid () {
        String votersId=voters_id.getText().toString();
        char[] charArray = votersId.toCharArray();
        for (int i = 0; i < 3; i++) {
            char ch = charArray[i];
            if (!(ch >= 'A' && ch <= 'Z')) {
                voters_id.setError("It is not a valid voter's id number");
                return false;
            }
            }
        for (int i = 3; i < charArray.length; i++) {
            char ch = charArray[i];
            if (!(ch >= '0' && ch <= '9')) {
                voters_id.setError("It is not a valid voter's id number");
                return false;
            }
        }
        if(votersId.isEmpty() ){
            voters_id.setError("It cannot be empty");
            return false;
        }
        else if(votersId.length()!=10){
            voters_id.setError("It should be 10-digits");
            return false;
        }
        else{
            voters_id.setError(null);
            return  true;
        }

    }
}