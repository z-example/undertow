package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.util.PathTemplateMatch;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class RouteExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();
        router.get("/", exchange -> {
            exchange.getResponseSender().send("Hello World!");
        });
        router.post("/user", exchange -> {
            exchange.getResponseSender().send("add user");
        });
        router.delete("/user/{id}", exchange -> {
            exchange.getResponseSender().send("update user");
        });
        router.put("/user", exchange -> {
            exchange.getResponseSender().send("update user");
        });
        router.get("/user/{id}", exchange -> {
            String id = exchange.getQueryParameters().get("id").peek();
            //path变量是必须的,如果不存在或者数据类型不对应该返回错误吗
            try {
                int userId = Integer.parseInt(id);
                exchange.getResponseSender().send("get user by id: " + userId);
            } catch (NumberFormatException e) {
                exchange.setStatusCode(404);
            }
        });

        //健康检查
        router.post("/health", exchange -> {
            exchange.getResponseSender().send("OK");
        });


        //用户等待返回结果的耗时任务
        router.get("/test", exchange -> {
            exchange.dispatch(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                exchange.getResponseSender().send("task");
            });
        });

        //某些异步任务, 服务器先接受, 之后再处理
        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
        router.get("/task", exchange -> {
            exchange.dispatch(taskExecutor,() -> {
                System.out.println("exec task");
            }).setStatusCode(202).getResponseSender().send("Accepted");
        });

        router.get("/file", exchange -> {
            exchange.dispatch(() -> {
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get("C:/文件夹大小.txt"));
                    exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
                } catch (Exception e) {
                    exchange.setStatusCode(500);
                }
            });
        });
        router.setFallbackHandler(exchange -> {
            exchange.getResponseSender().send("发生异常,执行服务降级(回退)处理");
        });

        HttpHandler handler = new SimpleErrorPageHandler(router);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(handler)
//                .setIoThreads(4)
//                .setWorkerThreads(16)
                .build();
        server.start();

    }

}
