package com.sample.ext;

import io.undertow.io.IoCallback;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.LocaleUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Zero on 2017/4/20.
 */
public class HttpContext {
    public static TemplateEngine engine;

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=utf-8";

    public final HttpServerExchange exchange;//delegate
    private Map<String, Object> data = new HashMap<>();
    private FormData formData;


    public HttpContext(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public void send(String str) {
        if (!exchange.getResponseHeaders().contains(Headers.CONTENT_TYPE)) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
        }
        exchange.getResponseSender().send(str, StandardCharsets.UTF_8, IoCallback.END_EXCHANGE);
    }

    public void render(String template) {
        exchange.dispatch(() -> {
            if (engine != null) {
                List<String> acceptLanguage = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE);
                List<Locale> ret = LocaleUtils.getLocalesFromHeader(acceptLanguage);

                exchange.getQueryParameters().forEach((k, v) -> {
                    if (!data.containsKey(k)) {
                        data.put(k, v.size() > 1 ? v : v.peek());
                    }
                });

                if (ret.isEmpty()) {
                    send(engine.render(template, data, Locale.US));
                } else {
                    send(engine.render(template, data, ret.get(0)));
                }
            } else {
                send("Error: Undefine template engine");
            }
        });
    }

    public String json(String json) {
        header(Headers.CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8);
        return json;
    }

    public String path() {
        return exchange.getRelativePath();
    }

    public String param(String name) {
        return exchange.getQueryParameters().get(name).peek();
    }

    public String path(String name) {
        return exchange.getQueryParameters().get(name).peek();
    }

    public HttpContext put(String name, Object value) {
        data.put(name, value);
        return this;
    }

    public String form(String name) {
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        Deque<FormData.FormValue> formValues = formData.get(name);
        return formValues == null || formValues.isEmpty() ? null : formValues.peek().getValue();
    }

    public FormData formData() {
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
//        if (formData == null) {
//            //throw
//        }
        return formData;
    }


    /**
     * Set response header
     *
     * @param headerName
     * @param headerValue
     * @return
     */

    public HttpContext header(HttpString headerName, String headerValue) {
        exchange.getResponseHeaders().put(headerName, headerValue);
        return this;
    }

    /**
     * Get request header
     *
     * @param headerName
     * @return
     */

    public String header(HttpString headerName) {
        return exchange.getRequestHeaders().get(headerName).peek();
    }

    /**
     * Get request header
     *
     * @param headerName
     * @return
     */
    public String header(String headerName) {
        return exchange.getRequestHeaders().get(headerName).peek();
    }


}
