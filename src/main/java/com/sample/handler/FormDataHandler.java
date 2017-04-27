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
public class FormDataHandler implements HttpHandler {


    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private FormParserFactory formParserFactory;
    private HttpHandler next;

    public FormDataHandler(HttpHandler next) {
        this(next, "utf-8");
    }

    public FormDataHandler(HttpHandler next, String defCharset) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(defCharset);
        FormParserFactory.Builder builder = FormParserFactory.builder();
        //默认ISO8859-1会导致文件名乱码问题
        builder.setDefaultCharset(defCharset);
        formParserFactory = builder.build();
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getAttachment(FormDataParser.FORM_DATA) == null) {
            String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
            if (contentType != null &&
                    (contentType.contains(MULTIPART_FORM_DATA) || contentType.equals(APPLICATION_X_WWW_FORM_URLENCODED))) {
                //FormDataParser不是线程安全的
                //FormEncodedDataParser和MultiPartUploadHandler
                FormDataParser parser = formParserFactory.createParser(exchange);
                parser.parse(next);
            } else {
                next.handleRequest(exchange);
            }
        } else {
            next.handleRequest(exchange);
        }
    }

}
