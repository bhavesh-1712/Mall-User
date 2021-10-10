package com.softwarehub.malluser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.UUID;

public class ScheduleTImeActivity extends AppCompatActivity {

    private String noMember;

    private TextView tvNoMember;
    private TextInputEditText etDate;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private AutoCompleteTextView etTimeSlot;
    private Button btnAvilibiltyCheck, btnBooked;
    private String date, timeslot;
    private int memberCount = 0;
    private String[] timeSlotList = {"10:00AM to 11:00AM",
            "11:00AM to 12:00PM",
            "12:00PM to 01:00PM",
            "01:00PM to 02:00PM",
            "02:00PM to 03:00PM",
            "03:00PM to 04:00PM",
            "04:00PM to 05:00PM",
            "05:00PM to 06:00PM",
            "06:00PM to 07:00PM"};

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference scheduleDatabaseRef;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_time);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        scheduleDatabaseRef = firebaseDatabase.getReference("SCHEDULE");

        dialog = new ProgressDialog(ScheduleTImeActivity.this);

        noMember = getIntent().getStringExtra("NO_MEMBER");
        tvNoMember = findViewById(R.id.tv_no_member_schedule);
        etDate = findViewById(R.id.et_date_schedule);
        etTimeSlot = findViewById(R.id.dropdown_menu_time);
        btnAvilibiltyCheck = findViewById(R.id.btn_check_avilability);
        btnBooked = findViewById(R.id.btn_book_slot);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, timeSlotList);
        etTimeSlot.setThreshold(1);
        etTimeSlot.setAdapter(adapter);

        tvNoMember.setText(noMember);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(ScheduleTImeActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                etDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
                datePickerDialog.show();
            }
        });
        btnBooked.setVisibility(View.GONE);
        etTimeSlot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                timeslot = timeSlotList[position];
            }
        });
        btnAvilibiltyCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = etDate.getText().toString();
                if (!TextUtils.isEmpty(date)) {
                    if (!TextUtils.isEmpty(timeslot)) {
                        checkAvailbilityOfTimeSlot(date, timeslot);
//                        if(checkAvailbilityOfTimeSlot(date,timeslot)){
//                            btnAvilibiltyCheck.setVisibility(View.GONE);
//                            btnBooked.setVisibility(View.VISIBLE);
//                        }else {
//                            Toast.makeText(ScheduleTImeActivity.this, "Please select another time slot", Toast.LENGTH_SHORT).show();
//                            etTimeSlot.setText(null);
//                            etDate.setText(null);
//                            btnAvilibiltyCheck.setVisibility(View.VISIBLE);
//                            btnBooked.setVisibility(View.GONE);
//                        }
                    } else {
                        etTimeSlot.setError("Please select timeslot");
                    }
                } else {
                    etDate.setError("Please select date");
                }
            }
        });
        btnBooked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookSlot(date, timeslot);
            }
        });
    }

    private void checkAvailbilityOfTimeSlot(String date, String timeslot) {
        dialog.setMessage("Checking...");
        dialog.show();

        scheduleDatabaseRef.child(date).child(timeslot).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    String members = dataSnapshot1.getValue().toString();
                    memberCount = Integer.parseInt(members) + memberCount;
                }
                manageCount(memberCount);
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MEMBER", error.getMessage());
                dialog.dismiss();
            }
        });
    }

    private void manageCount(int count) {
        if (count < 50) {
            Toast.makeText(ScheduleTImeActivity.this, "Slot Available", Toast.LENGTH_SHORT).show();
            btnAvilibiltyCheck.setVisibility(View.GONE);
            btnBooked.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(ScheduleTImeActivity.this, "Sorry! Please select another time slot", Toast.LENGTH_SHORT).show();
            etTimeSlot.setText(null);
            etDate.setText(null);
            btnAvilibiltyCheck.setVisibility(View.VISIBLE);
            btnBooked.setVisibility(View.GONE);
        }
    }

    private void bookSlot(String date, String timeslot) {
        dialog.setMessage("Booking slot..");
        dialog.show();

        scheduleDatabaseRef
                .child(date)
                .child(timeslot)
                .child(getFirebaseUid())
                .setValue(noMember)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        etDate.setText(null);
                        etTimeSlot.setText(null);
                        Toast.makeText(ScheduleTImeActivity.this, "Your time slot is booked", Toast.LENGTH_SHORT).show();
                        savedPreference(date,timeslot);
                    }
                });
    }

    private void savedPreference(String date,String timeslot){
        Utilities.getInstance().setPreference(this,SharedPreferenceKeys.user_id,getFirebaseUid());
        Utilities.getInstance().setPreference(this, SharedPreferenceKeys.date, date);
        Utilities.getInstance().setPreference(this, SharedPreferenceKeys.time_slot, timeslot);
    }

    private String getFirebaseUid() {
        return mAuth.getCurrentUser().getUid();
    }
}