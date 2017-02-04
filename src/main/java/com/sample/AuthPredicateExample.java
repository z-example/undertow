package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicate;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.util.Headers;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * 利用Predicate 处理授权问题
 *
 * @author Zero
 *         Created on 2017/2/4.
 */
public class AuthPredicateExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();


        router.get("/login", exchange -> {
            Deque<String> return_urlQ = exchange.getQueryParameters().get("return_url");
            if (return_urlQ == null) {

            }
            StringBuilder builder = new StringBuilder();
            builder.append("<html>");
            builder.append("<body>");
            builder.append("<form action='/login?return_url=/auth/1' method='post'>");
            builder.append("<input name='username' value='admin'> ");
            builder.append("<input type='password' name='password' value='123456'> ");
            builder.append("<input type='submit' value='Login'>");
            builder.append("</form>");
            builder.append("</body>");
            builder.append("</html>");
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
            exchange.getResponseSender().send(builder.toString());
        });

        router.post("/login", exchange -> {
            //sid用来处理session
            exchange.getResponseCookies().put("sid", new CookieImpl("sid", UUID.randomUUID().toString()));
            //token用来授权
            exchange.getResponseCookies().put("token", new CookieImpl("token", "role:admin ... jwt token... other"));
            Deque<String> return_url = exchange.getQueryParameters().get("return_url");
            if (return_url == null || return_url.isEmpty()) {
                Handlers.redirect("/auth/1").handleRequest(exchange);
            } else {
                Handlers.redirect(return_url.peek()).handleRequest(exchange);
            }
        });


        AuthPredicate authPredicate = new AuthPredicate();
        router.get("/auth/*", authPredicate, exchange -> {
            exchange.getResponseSender().send(exchange.getRelativePath());
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

    static class AuthPredicate implements Predicate {

        @Override
        public boolean resolve(HttpServerExchange exchange) {
            Cookie token = exchange.getRequestCookies().get("token");
            if (token == null) {
                //跳转到登录页面
                exchange.setStatusCode(302);
                exchange.getResponseHeaders().put(Headers.LOCATION, "http://localhost:8080/login?return_url=" + exchange.getRequestURL());
                return true;//直接返回false是提示404的
            } else {
                return true;
            }
        }
    }

}
