package com.sample.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

import java.util.Objects;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class FormLoginHandler implements HttpHandler {


    private HttpHandler next;

    public FormLoginHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
//        exchange.getSecurityContext().login()
    }

}
