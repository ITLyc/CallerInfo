package com.lyc.callerinfo.model.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;


import com.lyc.greendao.DaoSession;
import com.lyc.greendao.HyBeanDao;
import com.lyc.greendao.InCallBeanDao;

@Entity
public class InCallBean {
    /**
     * iszhapian : 1
     * province :
     * city : 上海
     * sp :
     * phone : 02151860253
     * rpt_type : 房产中介
     * rpt_comment : 房产中介
     * rpt_cnt : 24
     * hy : {"city":"上海","lng":"0","lat":"0","name":"上海xxxxxx有限公司","addr":"","tel":"021-51860253"}
     * hyname : 该号码所属公司名称
     * countDesc : 此号码近期被24位360手机卫士用户标记为房产中介电话！
     */
    @Id
    private Long id;
    private String iszhapian;
    private String province;
    private String city;
    private String sp;
    @NotNull
    private String phone;
    private String rpt_type;
    private String rpt_comment;
    private String rpt_cnt;
    private String hyname;
    private String countDesc;
    private Long hyId;
    @ToOne(joinProperty = "hyId")
    private HyBean bean;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1797035779)
    private transient InCallBeanDao myDao;
    @Generated(hash = 1056404411)
    public InCallBean(Long id, String iszhapian, String province, String city, String sp,
            @NotNull String phone, String rpt_type, String rpt_comment, String rpt_cnt, String hyname,
            String countDesc, Long hyId) {
        this.id = id;
        this.iszhapian = iszhapian;
        this.province = province;
        this.city = city;
        this.sp = sp;
        this.phone = phone;
        this.rpt_type = rpt_type;
        this.rpt_comment = rpt_comment;
        this.rpt_cnt = rpt_cnt;
        this.hyname = hyname;
        this.countDesc = countDesc;
        this.hyId = hyId;
    }
    @Generated(hash = 1822948020)
    public InCallBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getIszhapian() {
        return this.iszhapian;
    }
    public void setIszhapian(String iszhapian) {
        this.iszhapian = iszhapian;
    }
    public String getProvince() {
        return this.province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getCity() {
        return this.city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getSp() {
        return this.sp;
    }
    public void setSp(String sp) {
        this.sp = sp;
    }
    public String getPhone() {
        return this.phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getRpt_type() {
        return this.rpt_type;
    }
    public void setRpt_type(String rpt_type) {
        this.rpt_type = rpt_type;
    }
    public String getRpt_comment() {
        return this.rpt_comment;
    }
    public void setRpt_comment(String rpt_comment) {
        this.rpt_comment = rpt_comment;
    }
    public String getRpt_cnt() {
        return this.rpt_cnt;
    }
    public void setRpt_cnt(String rpt_cnt) {
        this.rpt_cnt = rpt_cnt;
    }
    public String getHyname() {
        return this.hyname;
    }
    public void setHyname(String hyname) {
        this.hyname = hyname;
    }
    public String getCountDesc() {
        return this.countDesc;
    }
    public void setCountDesc(String countDesc) {
        this.countDesc = countDesc;
    }
    public Long getHyId() {
        return this.hyId;
    }
    public void setHyId(Long hyId) {
        this.hyId = hyId;
    }
    @Generated(hash = 871179490)
    private transient Long bean__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Keep
    public HyBean getBean() {
        Long __key = this.hyId;
        if (bean__resolvedKey == null || !bean__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            HyBeanDao targetDao = daoSession.getHyBeanDao();
            HyBean beanNew = targetDao.load(__key);
            synchronized (this) {
                bean = beanNew;
                bean__resolvedKey = __key;
            }
        }
        return bean;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 264880732)
    public void setBean(HyBean bean) {
        synchronized (this) {
            this.bean = bean;
            hyId = bean == null ? null : bean.getId();
            bean__resolvedKey = hyId;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    @Override
    public String toString() {
        return "InCallBean{" +
                "id=" + id +
                ", iszhapian='" + iszhapian + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", sp='" + sp + '\'' +
                ", phone='" + phone + '\'' +
                ", rpt_type='" + rpt_type + '\'' +
                ", rpt_comment='" + rpt_comment + '\'' +
                ", rpt_cnt='" + rpt_cnt + '\'' +
                ", hyname='" + hyname + '\'' +
                ", countDesc='" + countDesc + '\'' +
                ", hyId=" + hyId +
                ", bean=" + bean +
                ", daoSession=" + daoSession +
                ", myDao=" + myDao +
                ", bean__resolvedKey=" + bean__resolvedKey +
                '}';
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 530429740)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getInCallBeanDao() : null;
    }
}
