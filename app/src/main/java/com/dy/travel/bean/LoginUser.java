package com.dy.travel.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LoginUser implements Serializable {

    /**
     * 登录用户信息
     */
    private static final long serialVersionUID = -6534476708111889922L;
    private Integer id;
    private String passwd;// 密码
    private String phone;

    private Map<String, List<String>> cookieMap;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, List<String>> getCookieMap() {
        return cookieMap;
    }

    public void setCookieMap(Map<String, List<String>> cookieMap) {
        this.cookieMap = cookieMap;
    }


}