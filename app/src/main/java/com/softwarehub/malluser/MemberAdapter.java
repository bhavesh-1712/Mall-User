package com.softwarehub.malluser;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder>{
    private List<MemberModel> memberModelList;
    private Activity activity;

    public MemberAdapter(List<MemberModel> memberModelList, Activity activity) {
        this.memberModelList = memberModelList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_member_layout, parent, false);

        return new MemberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  MemberAdapter.ViewHolder holder, int position) {
        String name = memberModelList.get(position).getName();
        String age = memberModelList.get(position).getAge();
        String gender = memberModelList.get(position).getGender();

        holder.setMember(name,age,gender);
    }

    @Override
    public int getItemCount() {
        return memberModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName,tvAge,tvGender;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_member_name);
            tvAge = itemView.findViewById(R.id.tv_member_age);
            tvGender = itemView.findViewById(R.id.tv_member_gender);
        }

        private void setMember(String name,String age,String gender){
            tvName.setText(name);
            tvAge.setText(age);
            tvGender.setText(gender);
        }
    }
}
