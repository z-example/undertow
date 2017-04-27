package com.sample.ext.v1;

import io.undertow.server.HttpServerExchange;

/**
 * Created by Zero on 2017/4/17.
 */
public class Request {
    public final HttpServerExchange exchange;//delegate

    public Request(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public String param(String name) {
        return exchange.getQueryParameters().get(name).peekFirst();
    }


}
