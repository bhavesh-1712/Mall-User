package com.softwarehub.malluser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeUsername,tvNoMember,tvBookSlotHeading,tvBookSlot;
    private Button btnAddMember,btnScheduleTime;
    private RecyclerView rvMemberList;
    private List<MemberModel> memberModelList;
    private MemberAdapter memberAdapter;

    private Dialog addMemberDialog;
    private TextInputEditText etNameDialog,etAgeDialog;
    private AutoCompleteTextView etGenderDialog;
    private String[] genderList = { "Male","Female","Other"};
    private String name,age,gender;
    private Button btnAddMemberDialog;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userDatabaseRef;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseRef = firebaseDatabase.getReference("USERS");

        dialog = new ProgressDialog(MainActivity.this);

        tvWelcomeUsername = findViewById(R.id.tv_welcome_username);
        tvNoMember = findViewById(R.id.tv_no_member);
        tvBookSlot = findViewById(R.id.tv_booked_slot);
        tvBookSlotHeading = findViewById(R.id.tv_booked_slot_head);
        btnAddMember = findViewById(R.id.btn_add_member);
        btnScheduleTime = findViewById(R.id.btn_schedule_time);
        rvMemberList = findViewById(R.id.rv_member_list);
        memberModelList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvMemberList.setLayoutManager(linearLayoutManager);
        memberAdapter = new MemberAdapter(memberModelList,this);
        rvMemberList.setAdapter(memberAdapter);

        tvWelcomeUsername.setText("Welcome, "+mAuth.getCurrentUser().getEmail());

        /////////////////////////Add Member Dialog Start////////////////////////////////
        addMemberDialog = new Dialog(MainActivity.this);
        addMemberDialog.setContentView(R.layout.add_member_layout_dialog);
        addMemberDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.et_bg));
        addMemberDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addMemberDialog.setCancelable(true);
        etNameDialog = addMemberDialog.findViewById(R.id.et_name);
        etAgeDialog = addMemberDialog.findViewById(R.id.et_age);
        etGenderDialog = addMemberDialog.findViewById(R.id.dropdown_menu_gender);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, genderList);
        etGenderDialog.setThreshold(1);
        etGenderDialog.setAdapter(adapter);
        btnAddMemberDialog = addMemberDialog.findViewById(R.id.btn_add_member);
        btnAddMemberDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etNameDialog.getText().toString();
                age = etAgeDialog.getText().toString();

                if(!TextUtils.isEmpty(name)){
                    if(!TextUtils.isEmpty(age)){
                        if(Integer.parseInt(age) < 99){
                            if(!TextUtils.isEmpty(gender)){
                                addUser(name,age,gender);
                            }else {
                                etGenderDialog.setError("Please select gender");
                            }
                        }else {
                            etAgeDialog.setError("Please enter valid age");
                        }
                    }else {
                        etAgeDialog.setError("Fill blank Field");
                    }
                }else {
                    etNameDialog.setError("Fill blank Field");
                }
            }
        });
        etGenderDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gender = genderList[position];
            }
        });
        /////////////////////////Add Member Dialog End////////////////////////////////

        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMemberDialog.show();
            }
        });

        btnScheduleTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = Utilities.getInstance().getPreference(getApplicationContext(),SharedPreferenceKeys.date);
                if(TextUtils.isEmpty(date)){
                    Intent intent = new Intent(MainActivity.this,ScheduleTImeActivity.class);
                    intent.putExtra("NO_MEMBER",String.valueOf(memberModelList.size()));
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "Sorry! You can't book another time slot", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getMember();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent registerIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(registerIntent);
            finish();
        } else {
            String date =  Utilities.getInstance().getPreference(getApplicationContext(),SharedPreferenceKeys.date);
            String timeslot = Utilities.getInstance().getPreference(getApplicationContext(),SharedPreferenceKeys.time_slot);
            if(TextUtils.isEmpty(date)){
                tvBookSlot.setVisibility(View.GONE);
                tvBookSlotHeading.setVisibility(View.GONE);
            }else {
                tvBookSlot.setVisibility(View.VISIBLE);
                tvBookSlotHeading.setVisibility(View.VISIBLE);
                tvBookSlot.setText(date + " " + timeslot);
                try {
                    long diff = getFormatedDays(date);
                    if (diff < 0) {
                        Utilities.getInstance().removePreference(MainActivity.this, SharedPreferenceKeys.date);
                        Utilities.getInstance().removePreference(MainActivity.this, SharedPreferenceKeys.time_slot);
                        Utilities.getInstance().removePreference(MainActivity.this, SharedPreferenceKeys.user_id);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("DATE_ERROR", e.getMessage());
                }
            }
        }
    }

    private long getFormatedDays(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        Date endDate = format.parse(date);
        Date currDate = new Date();

        long difference = endDate.getTime() - currDate.getTime();

        return difference;
    }

    private void getMember(){
        dialog.setMessage("Loading...");
        dialog.show();
        memberModelList.clear();
        userDatabaseRef.child(getFirebaseUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    memberModelList.add(dataSnapshot1.getValue(MemberModel.class));
                }
                memberAdapter.notifyDataSetChanged();
                if(memberModelList.size() > 0){
                    rvMemberList.setVisibility(View.VISIBLE);
                    tvNoMember.setVisibility(View.INVISIBLE);
                    btnScheduleTime.setClickable(true);
                }else {
                    rvMemberList.setVisibility(View.INVISIBLE);
                    tvNoMember.setVisibility(View.VISIBLE);
                    btnScheduleTime.setClickable(false);
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MEMBER", error.getMessage());
                dialog.dismiss();
            }
        });
    }

    private void addUser(String name, String age, String gender){
        addMemberDialog.dismiss();
        dialog.setMessage("Adding User");
        dialog.show();
        MemberModel memberModel = new MemberModel(name,age,gender);
        userDatabaseRef
                .child(getFirebaseUid())
                .child(getUUID())
                .setValue(memberModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        etNameDialog.setText(null);
                        etAgeDialog.setText(null);
                        etGenderDialog.setText(null);
                        getMember();
                    }
                });
    }

    private String getUUID(){
       return UUID.randomUUID().toString();
    }

    private String getFirebaseUid(){
        return mAuth.getCurrentUser().getUid();
    }
}