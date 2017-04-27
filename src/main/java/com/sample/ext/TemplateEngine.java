package com.sample.ext;

import com.sample.util.Reference;

import java.util.Locale;
import java.util.Map;

/**
 * Created by Zero on 2017/4/18.
 */
public interface TemplateEngine {

    Reference<TemplateEngine> defalut = new Reference<>();

    //http://vertx.io/docs/vertx-web/java/
    String render(String template, Map<String, Object> data, Locale locale);



}
