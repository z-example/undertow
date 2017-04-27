package com.sample.ext.v1;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Created by Zero on 2017/4/19.
 */
public interface RequestHandlerV1 extends HttpHandler{

    @Override
    default void handleRequest(HttpServerExchange exchange) throws Exception {
        handleRequest(new Request(exchange), new Response(exchange));
    }

    static HttpHandler adpt(RequestHandlerV1 handler) {
        return handler;
    }

    void handleRequest(Request request, Response response);

}
