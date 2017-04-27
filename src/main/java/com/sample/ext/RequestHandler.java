package com.sample.ext;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Created by Zero on 2017/4/19.
 */
public interface RequestHandler extends HttpHandler{

    @Override
    default void handleRequest(HttpServerExchange exchange) throws Exception {
        handleRequest(new HttpContext(exchange));
    }

    static HttpHandler adpt(RequestHandler handler) {
        return handler;
    }

    void handleRequest(HttpContext context);

}
