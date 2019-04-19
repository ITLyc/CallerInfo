package com.lyc.callerinfo.model.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class HyBean {
    @Id
    private long id;
    private String city;
    private String lng;
    private String lat;
    private String name;
    private String addr;
    private String tel;
    @Generated(hash = 49284897)
    public HyBean(long id, String city, String lng, String lat, String name,
            String addr, String tel) {
        this.id = id;
        this.city = city;
        this.lng = lng;
        this.lat = lat;
        this.name = name;
        this.addr = addr;
        this.tel = tel;
    }
    @Generated(hash = 1785153980)
    public HyBean() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getCity() {
        return this.city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getLng() {
        return this.lng;
    }
    public void setLng(String lng) {
        this.lng = lng;
    }
    public String getLat() {
        return this.lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddr() {
        return this.addr;
    }
    public void setAddr(String addr) {
        this.addr = addr;
    }
    public String getTel() {
        return this.tel;
    }
    public void setTel(String tel) {
        this.tel = tel;
    }

    @Override
    public String toString() {
        return "HyBean{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", lng='" + lng + '\'' +
                ", lat='" + lat + '\'' +
                ", name='" + name + '\'' +
                ", addr='" + addr + '\'' +
                ", tel='" + tel + '\'' +
                '}';
    }
}
