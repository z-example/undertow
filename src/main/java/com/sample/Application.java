package com.sample;

import com.sample.handler.FormDataHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.PathTemplateMatch;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Zero
 *         Created on 2017/2/2.
 */
public class Application {
    //ab -n 100 -c 10 http://127.0.0.1:8080/
    public static void main(String[] args) throws ServletException {

        RoutingHandler webRouter = Handlers.routing();
        webRouter.get("/", exchange -> {
            //不要在这里写阻塞代码
            exchange.getResponseSender().send("Hello World");//send(data, IoCallback.END_EXCHANGE);
        });
        webRouter.get("/nonsend", exchange -> {
            System.out.println("会自动关闭");
        });

        webRouter.post("/form", exchange -> {
            //这里使用了封装好的FormDataHandler，省略下面代码
           /* FormParserFactory.Builder builder = FormParserFactory.builder();
            FormDataParser parser = builder.build().createParser(exchange);
            FormData formData = parser.parseBlocking();
            exchange.getResponseSender().send(formData.toString());*/

            FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
            exchange.getResponseSender().send(formData.toString());
        });

        webRouter.get("/send", exchange -> {
            exchange.getResponseSender().send("send 1", new IoCallback() {
                @Override
                public void onComplete(HttpServerExchange exchange, Sender sender) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                    sender.send("send 2");
                }

                @Override
                public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {

                }
            });
        });

        //metrics
        webRouter.get("/metrics", exchange -> {
            Thread.sleep(3000);
            exchange.getResponseSender().send("Hello Metrics");
        });

        //同步
        webRouter.post("upload", new BlockingHandler(exchange -> {
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
        webRouter.post("upload2", exchange -> {
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
        webRouter.post("formdata", Helper.formParserHandler(exchange -> {
            FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
            exchange.getResponseSender().send(formData.toString());
        }));
        //表单提交和文件长传都可以
        webRouter.post("formdata2", exchange -> {
            FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
            exchange.getResponseSender().send(formData.toString());
        });


        //静态资源(jar中) 访问:http://localhost:8080/static/img.jpg
        //ResourceHandler第288行,
        ResourceManager resourceManager = new ClassPathResourceManager(Application.class.getClassLoader());//CachingResourceManager
        webRouter.get("/static/*", Handlers.resource(resourceManager));

//----------------------------------------------REST----------------------------------------------------
        RoutingHandler api = Handlers.routing();
        //User API
        api.post("/user", exchange -> {
            exchange.getResponseSender().send("add user");
        });
        api.delete("/user/{id}", exchange -> {
            exchange.getResponseSender().send("update user");
        });
        api.put("/user", exchange -> {
            exchange.getResponseSender().send("update user");
        });
        //通过getQueryParameters()获取路径变量
        api.get("/user/{id}", exchange -> {
            String id = exchange.getQueryParameters().get("id").peek();
            //path变量是必须的,如果不存在或者数据类型不对应该返回错误吗
            try {
                int userId = Integer.parseInt(id);
                exchange.getResponseSender().send("get user by id: " + userId);
            } catch (NumberFormatException e) {
                exchange.setStatusCode(404);
            }
        });
        //通过PathTemplateMatch获取路径变量
        api.get("/{test}/*", exchange -> {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String itemId1 = pathMatch.getParameters().get("test"); // or exchange.getQueryParameters().get("test")
            String itemId2 = pathMatch.getParameters().get("*"); // or exchange.getQueryParameters().get("*")
            System.out.println(itemId1);
            System.out.println(itemId2);
            exchange.getResponseSender().send("itemId1:" + itemId1 + " \nitemId2" + itemId2);
        });
        //----------------------------------------------Web Socket----------------------------------------------------
        PathHandler wsHandler = Handlers.path();
        wsHandler.addPrefixPath("/chat", Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                    WebSockets.sendText(message.getData(), channel, null);
                }
            });
            channel.resumeReceives();
        }));
        //http://localhost:8080/api/ws/chatUI/
        wsHandler.addPrefixPath("/chatUI", Handlers.resource(new ClassPathResourceManager(Application.class.getClassLoader())).addWelcomeFiles("ws.html"));

        //Undertow匹配上了一个不会继续匹配,需要代码显示传递并调用next handler
        //不用担心
        api.get("err", exchange -> {
            throw new RuntimeException("CCF");
        });
        //HttpHandler链
        HttpHandler webHandler = new MetricsHandler(webRouter);//router是MetricsHandler的next handler
        webHandler = new SimpleErrorPageHandler(webHandler);//错误处理
        webHandler = new FormDataHandler(webHandler);//加上这句之后,就可以自动处理所有formdata了
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.path()
                        .addPrefixPath("/", webHandler)
                        .addPrefixPath("/api", api)
                        .addPrefixPath("/api/ws", wsHandler)
                )
                .setIoThreads(4)
                .build();
        server.start();
        //http://localhost:8080/
        //http://localhost:8080/api/user/123

//        Async.executor = server.getWorker();
        //Worker Thread
        server.getWorker().submit(() -> {
            System.out.println("First Task: " + Thread.currentThread().getName());//First Task: XNIO-1 task-1
        });
    }
}
