package com.lyc.callerinfo.model.bean;

import java.io.Serializable;

public class InCallModel implements Serializable {


    /**
     * reason : 查询成功
     * result : {"iszhapian":0,"province":"北京","city":"北京","sp":"中国联通","phone":"18511122016","rpt_type":"","rpt_comment":"","rpt_cnt":"","hy":null,"countDesc":"","hyname":""}
     * error_code : 0
     */

    private String reason;
    private ResultBean result;
    private int error_code;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public static class ResultBean {
        /**
         * iszhapian : 0
         * province : 北京
         * city : 北京
         * sp : 中国联通
         * phone : 18511122016
         * rpt_type :
         * rpt_comment :
         * rpt_cnt :
         * hy : null
         * countDesc :
         * hyname :
         */

        private int iszhapian;
        private String province;
        private String city;
        private String sp;
        private String phone;
        private String rpt_type;
        private String rpt_comment;
        private String rpt_cnt;
        private Object hy;
        private String countDesc;
        private String hyname;

        public int getIszhapian() {
            return iszhapian;
        }

        public void setIszhapian(int iszhapian) {
            this.iszhapian = iszhapian;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getSp() {
            return sp;
        }

        public void setSp(String sp) {
            this.sp = sp;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRpt_type() {
            return rpt_type;
        }

        public void setRpt_type(String rpt_type) {
            this.rpt_type = rpt_type;
        }

        public String getRpt_comment() {
            return rpt_comment;
        }

        public void setRpt_comment(String rpt_comment) {
            this.rpt_comment = rpt_comment;
        }

        public String getRpt_cnt() {
            return rpt_cnt;
        }

        public void setRpt_cnt(String rpt_cnt) {
            this.rpt_cnt = rpt_cnt;
        }

        public Object getHy() {
            return hy;
        }

        public void setHy(Object hy) {
            this.hy = hy;
        }

        public String getCountDesc() {
            return countDesc;
        }

        public void setCountDesc(String countDesc) {
            this.countDesc = countDesc;
        }

        public String getHyname() {
            return hyname;
        }

        public void setHyname(String hyname) {
            this.hyname = hyname;
        }
    }
}
