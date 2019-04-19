package com.lyc.callerinfo.model.db;

import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;

@Entity
public class InCall {
    @Id
    private long id;
    String number;
    long time;
    long ringTime;
    long duration;
    boolean isExpanded = false;

    @Keep
    public InCall(String number, long time, long ringTime, long duration) {
        if (!TextUtils.isEmpty(number)) {
            number = number.replaceAll(" ", "");
        }
        this.number = number;
        this.time = time;
        this.ringTime = ringTime;
        this.duration = duration;
    }

    @Generated(hash = 1478303842)
    public InCall() {
    }

    @Generated(hash = 626206788)
    public InCall(long id, String number, long time, long ringTime, long duration,
            boolean isExpanded) {
        this.id = id;
        this.number = number;
        this.time = time;
        this.ringTime = ringTime;
        this.duration = duration;
        this.isExpanded = isExpanded;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getRingTime() {
        return this.ringTime;
    }

    public void setRingTime(long ringTime) {
        this.ringTime = ringTime;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean getIsExpanded() {
        return this.isExpanded;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    @Override
    public String toString() {
        return "InCall{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", time=" + time +
                ", ringTime=" + ringTime +
                ", duration=" + duration +
                ", isExpanded=" + isExpanded +
                '}';
    }
}
