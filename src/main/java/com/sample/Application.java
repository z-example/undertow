package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.PathTemplateMatch;

import javax.servlet.ServletException;
import java.util.Deque;

/**
 * @author Zero
 *         Created on 2017/2/2.
 */
public class Application {
    //ab -n 100 -c 10 http://127.0.0.1:8080/hh
    public static void main(String[] args) throws ServletException {
        //setIoThreads(20)
        //>ab -n 10000 -c 10000 http://127.0.0.1:8080/hh 68s
        //>ab -n 10000 -c 1000 http://127.0.0.1:8080/hh 8.8s
        //>ab -n 10000 -c 100 http://127.0.0.1:8080/hh 5s
//        PathHandler path = Handlers.path();
//        path.addPrefixPath("/hh", new HttpHandler() {
//            @Override
//            public void handleRequest(HttpServerExchange exchange) throws Exception {
////                        exchange.getResponseHeaders().put(Headers.STATUS, 200);
////                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
//                System.out.println(Thread.currentThread().getName());
//                exchange.getResponseSender().send("Hello World");
//            }
//        });

        RoutingHandler router = Handlers.routing();

        router.get("hello", exchange -> {
            exchange.getResponseSender().send("Hello World");
        });
        router.get("metrics", exchange -> {
            Thread.sleep(3000);
            exchange.getResponseSender().send("Hello Metrics");
        });
        router.get("/user/{id}", exchange -> {
            exchange.getResponseSender().send("User id:" + exchange.getQueryParameters().get("id").peek());
        });
        router.get("/{test}/*", exchange -> {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String itemId1 = pathMatch.getParameters().get("test"); // or exchange.getQueryParameters().get("test")
            String itemId2 = pathMatch.getParameters().get("*"); // or exchange.getQueryParameters().get("*")
            System.out.println(itemId1);
            System.out.println(itemId2);
            exchange.getResponseSender().send("itemId1:" + itemId1 + " \nitemId2" + itemId2);
        });
        //同步
        router.post("upload", new BlockingHandler(exchange -> {
            FormParserFactory.Builder builder = FormParserFactory.builder();
            builder.setDefaultCharset("UTF-8");//默认ISO8859-1会导致文件名乱码问题
            FormDataParser parser = builder.build().createParser(exchange);
            FormData formData = parser.parseBlocking();
            parser.parse(exchange1 -> {
            });
            FormData.FormValue file = formData.getFirst("file");
            System.out.println(Thread.currentThread().getName());
            System.out.println(file.getFileName());
            exchange.getResponseSender().send(file.getFileName());
        }));
        //异步,Undertow的异步处理结果是放在附件中的,可以通过exchange.getAttachment()获得结果
        router.post("upload2", exchange -> {
            FormParserFactory.Builder builder = FormParserFactory.builder();
            builder.setDefaultCharset("UTF-8");//默认ISO8859-1会导致文件名乱码问题
            FormDataParser parser = builder.build().createParser(exchange);
            parser.parse(exchange1 -> {
                FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
                FormData.FormValue file = formData.getFirst("file");
                System.out.println(Thread.currentThread().getName());
                System.out.println(file.getFileName());
                exchange.getResponseSender().send(file.getFileName());
            });
        });

        router.get("err", exchange -> {
            throw new RuntimeException("CCF");
        });
        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(4)
                .build();
        server.start();
    }
}
