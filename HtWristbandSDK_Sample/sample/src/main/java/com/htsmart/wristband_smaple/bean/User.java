package com.htsmart.wristband_smaple.bean;

import com.htsmart.wristband.bean.IWristbandUser;

import java.util.Date;

/**
 * Created by Kilnn on 16-10-17.
 */
public class User implements IWristbandUser {

    private int id;
    private int height;
    private int weight;
    private Date birthday;
    private boolean wearLeft;
    private boolean sex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public boolean isWearLeft() {
        return wearLeft;
    }

    public void setWearLeft(boolean wearLeft) {
        this.wearLeft = wearLeft;
    }

    @Override
    public String wristbandUserId() {
        return  String.valueOf(id);
    }

    @Override
    public int wristbandHeight() {
        return height;
    }

    @Override
    public int wristbandWeight() {
        return weight;
    }

    @Override
    public Date wristbandBirthday() {
        return birthday;
    }

    @Override
    public boolean wristbandSex() {
        return sex;
    }

    @Override
    public boolean wristbandWearLeft() {
        return wearLeft;
    }

    @Override
    public int wristbandDiastolicPressure() {
        return 0;
    }

    @Override
    public int wristbandSystolicPressure() {
        return 0;
    }
}
