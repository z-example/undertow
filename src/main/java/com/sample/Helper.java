package com.sample;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.PathTemplateMatch;

import java.util.Objects;

/**
 * @author Zero
 *         Created on 2017/2/3.
 */
public final class Helper {

    public static void render(HttpServerExchange exchange, String view) {
//        exchange.getRequestReceiver().
    }

    public static FormParserFactory formParserFactory(){
        FormParserFactory.Builder builder = FormParserFactory.builder();
        builder.setDefaultCharset("UTF-8");//默认ISO8859-1会导致文件名乱码问题
        return builder.build();
    }



    public static String pathStr(HttpServerExchange exchange, String name, String defValue) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        return pathMatch.getParameters().getOrDefault(name,defValue);
    }

    public static int pathInt(HttpServerExchange exchange, String name, int defValue) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String v = pathMatch.getParameters().get(name);
        if (v == null) {
            return defValue;
        }
        return Integer.parseInt(v);
    }



}
