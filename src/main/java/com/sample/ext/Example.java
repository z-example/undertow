package com.sample.ext;

import com.sample.ext.impl.ThymeleafEngineImpl;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;

/**
 * 当前例子不依赖Servlet
 * @author Zero
 *         Created on 2017/2/2.
 */
public class Example {
    //ab -n 100 -c 10 http://127.0.0.1:8080/hh
    public static void main(String[] args) throws Exception {
        //>ab -n 100000 -c 100 http://localhost:8080/name 68s

        RoutingHandler router = Handlers.routing();
        router.get("/", (RequestHandler) context -> {
            context.send("Hello world");
        });

        router.get("/test", RequestHandler.adpt(Example::test));

        router.get("/hello", exchange -> {
            exchange.getResponseSender().send("Hello World!");
        });

        HttpContext.engine = new ThymeleafEngineImpl();
        TemplateEngine.defalut.set(HttpContext.engine);

        //Handlers.responseRateLimitingHandler(router) 下载限速
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(new SimpleErrorPageHandler(router))
                .setWorkerThreads(4)
                .setIoThreads(2)
                .build();
        server.start();
    }

    public static void test(HttpContext context) {
        context.put("msg", "hello world");
        context.render("/test");
    }
}
