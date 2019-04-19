package com.lyc.callerinfo.model.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MarkedRecord {
    public final static int API_ID_USER_MARKED = 8;
    public final static int TYPE_IGNORE = 32;
    @Id(autoincrement = true)
    private long id;
    private String number;
    private String uid;
    private int type;
    private long time;
    private int count;
    private int source;
    private boolean reported;
    private String typeName;

    @Generated(hash = 1463709856)
    public MarkedRecord(long id, String number, String uid, int type, long time,
            int count, int source, boolean reported, String typeName) {
        this.id = id;
        this.number = number;
        this.uid = uid;
        this.type = type;
        this.time = time;
        this.count = count;
        this.source = source;
        this.reported = reported;
        this.typeName = typeName;
    }

    @Generated(hash = 1295442165)
    public MarkedRecord() {
    }

    public boolean isIgnore() {
        return this.type == TYPE_IGNORE;
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

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public boolean getReported() {
        return this.reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "MarkedRecord{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", uid='" + uid + '\'' +
                ", type=" + type +
                ", time=" + time +
                ", count=" + count +
                ", source=" + source +
                ", reported=" + reported +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
