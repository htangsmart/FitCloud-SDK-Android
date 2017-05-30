package com.htsmart.wristband.sample.syncdata.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Kilnn on 2017/3/27.
 */
@Entity(indexes = {
        @Index(value = "userId,type,time", unique = true)
})
public class SyncRawDataEntity {

    @Id
    private Long id;

    private long userId;

    private int type;// data type

    private int time;

    private int value;

    private int value2;


    @Generated(hash = 713880727)
    public SyncRawDataEntity(Long id, long userId, int type, int time, int value,
            int value2) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.time = time;
        this.value = value;
        this.value2 = value2;
    }

    @Generated(hash = 1948039460)
    public SyncRawDataEntity() {
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue2() {
        return this.value2;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

}
