package com.example.ruiji.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Res<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;
    private T data;
    private Map<String, Object> map = new HashMap<>();

    public static <T> Res<T> success(T object) {
        Res<T> res = new Res<>();
        res.data = object;
        res.code = 1;
        return res;
    }

    public static <T> Res<T> error(String msg) {
        Res<T> res = new Res<>();
        res.msg = msg;
        res.code = 0;
        return res;
    }

    public Res<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}
