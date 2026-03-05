package com.example.ruiji.common;

import com.baomidou.mybatisplus.extension.api.R;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Res<T> {
    private Integer code;
    private  String msg;
    private  T data;
    private Map map=new HashMap();

    public static  <T> Res<T> success(T object){
        Res<T> res = new Res<T>();
        res.data = object;
        res.code = 1;
        return res;
    }

    public static  <T> Res<T> error(String msg){
        Res res = new Res();
        res.msg = msg;
        res.code = 0;
        return res;
    }

    public Res<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}
