package com.sample.util;

/**
 * Created by Zero on 2017/4/23.
 */
public class Reference<T> {

    private T object;

    public void set(T obj) {
        this.object = obj;
    }

    public T get() {
        return this.object;
    }

}
