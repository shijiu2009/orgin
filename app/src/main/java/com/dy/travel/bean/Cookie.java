package com.dy.travel.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Cookie implements Serializable {

    /**
     * 存储cookie
     */
    private static final long serialVersionUID = -9097708418275243256L;

    private Map<String, List<String>> cookieMap;

    public Map<String, List<String>> getCookieMap() {
        return cookieMap;
    }

    public void setCookieMap(Map<String, List<String>> cookieMap) {
        this.cookieMap = cookieMap;
    }

}