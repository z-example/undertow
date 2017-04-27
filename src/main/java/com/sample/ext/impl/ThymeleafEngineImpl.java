package com.sample.ext.impl;

import com.sample.ext.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Zero on 2017/4/22.
 */
public class ThymeleafEngineImpl implements TemplateEngine {

    static org.thymeleaf.TemplateEngine engine = new org.thymeleaf.TemplateEngine();

    static {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // HTML is the default mode, but we set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // This will convert "home" to "/templates/home.html"
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        // Template cache TTL=1h. If not set, entries would be cached until expelled by LRU
        templateResolver.setCacheTTLMs(3600000L);

        // Cache is set to true by default. Set to false if you want templates to
        // be automatically updated when modified.
        templateResolver.setCacheable(true);
        engine.setTemplateResolver(templateResolver);
    }

    @Override
    public String render(String template, Map<String, Object> data, Locale locale) {
        return engine.process(template, new IContext() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public boolean containsVariable(String name) {
                return data.containsKey(name);
            }

            @Override
            public Set<String> getVariableNames() {
                return data.keySet();
            }

            @Override
            public Object getVariable(String name) {
                return data.get(name);
            }
        });
    }
}
