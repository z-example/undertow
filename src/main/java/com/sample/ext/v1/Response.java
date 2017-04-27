package com.sample.ext.v1;

import io.undertow.server.HttpServerExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zero on 2017/4/17.
 */
public class Response {
    public final HttpServerExchange exchange;//delegate

    private Map<String,Object> data=new HashMap<>();

    public Response(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public void end(String str) {
        exchange.getResponseSender().send(str);
    }

}
