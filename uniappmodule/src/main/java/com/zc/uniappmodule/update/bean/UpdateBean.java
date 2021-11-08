package com.zc.uniappmodule.update.bean;

public class UpdateBean {

    private Integer code;
    private String msg;
    private DataBean data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String uniappName;
        private String uniappid;
        private Integer versionCode;
        private String versionName;
        private String download;

        public String getUniappName() {
            return uniappName;
        }

        public void setUniappName(String uniappName) {
            this.uniappName = uniappName;
        }

        public String getUniappid() {
            return uniappid;
        }

        public void setUniappid(String uniappid) {
            this.uniappid = uniappid;
        }

        public Integer getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(Integer versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getDownload() {
            return download;
        }

        public void setDownload(String download) {
            this.download = download;
        }
    }
}
