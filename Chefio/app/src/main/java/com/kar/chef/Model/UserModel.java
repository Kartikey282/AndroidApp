package com.kar.chef.Model;

public class UserModel {
    private String uid, name, address,phone,building;

    public UserModel() {

    }

    public UserModel(String uid, String name, String address,String building) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.building = building;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
