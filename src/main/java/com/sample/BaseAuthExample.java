package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.util.Headers;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class BaseAuthExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();


        router.get("/admin/*", exchange -> {
            exchange.getResponseSender().send(exchange.getRelativePath());
        });

        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        handler = new BaseAuthHandler(handler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(4)
                .build();
        server.start();
    }

    static class BaseAuthHandler implements HttpHandler {
        private HttpHandler next;
        private List<String> whitelist = new ArrayList<>();

        public BaseAuthHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            //浏览器收到未授权头消息时,会弹出登录对话框,然后添加授权信息到header中在此请求改页面
            //登出是个问题,授权后信息是缓存在浏览器中的,之后每次请求都会带上授权信息(清除cookie是不起作用的)
            if (whitelist.contains(exchange.getRelativePath())) {
                next.handleRequest(exchange);
            } else {
                String authorization = exchange.getRequestHeaders().getFirst(Headers.AUTHORIZATION);
                if (authorization == null) {
                    exchange.getResponseHeaders().put(Headers.WWW_AUTHENTICATE, "Basic realm=\"Secure Area\"");
                    exchange.setStatusCode(401);
                } else {
                    String[] split = authorization.split(" ");
                    if (split.length == 2) {
                        authorization = new String(Base64.getDecoder().decode(split[1]), "iso8859-1");
                        //这里省略验证合法性
                        System.out.println(authorization);
                    }
                    next.handleRequest(exchange);
                }
            }
        }

    }


}
