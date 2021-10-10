package com.softwarehub.malluser;

public class MemberModel {
    String name;
    String age;
    String gender;

    public MemberModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public MemberModel(String name,String age,String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
