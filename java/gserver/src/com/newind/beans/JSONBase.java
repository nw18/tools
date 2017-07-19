package com.newind.beans;

import com.alibaba.fastjson.JSON;

/**
 * Created by newind on 17-7-19.
 */
public class JSONBase {
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
