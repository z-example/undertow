package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicate;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.util.Headers;

import java.security.Principal;
import java.util.*;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class FormLoginExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();

        router.get("/login", exchange -> {
            StringBuilder builder = new StringBuilder();
            builder.append("<html>");
            builder.append("<body>");
            builder.append("<form action='/login?return_url=/admin/1' method='post'>");
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
            String return_url = exchange.getQueryParameters().get("return_url").peek();
            if (return_url == null) {
                return_url = "/admin/1";
            }
            Handlers.redirect(return_url).handleRequest(exchange);
//            exchange.getResponseHeaders().put(Headers.LOCATION, return_url);
        });

        router.get("/admin/*", exchange -> {
            exchange.getResponseSender().send(exchange.getRelativePath());
        });

        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        handler = new SecurityHandler(handler).addPublicPath("/login");
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(4)
                .build();
        server.start();
    }

    static class SecurityHandler implements HttpHandler {
        private HttpHandler next;
        private List<String> whitelist = new ArrayList<>();

        public SecurityHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            if (whitelist.contains(exchange.getRelativePath())) {
                next.handleRequest(exchange);
            } else {
                Cookie token = exchange.getRequestCookies().get("token");
                if (token == null) {
                    //跳转到登录页面
                    exchange.setStatusCode(302);
//                    exchange.setRequestPath("/login");
//                    exchange.setRelativePath("/login");
                    exchange.getResponseHeaders().put(Headers.LOCATION, "http://localhost:8080/login?return_url=" + exchange.getRequestURL());
                } else {
                    next.handleRequest(exchange);
                }
            }
        }

        public SecurityHandler addPublicPath(String path) {
            whitelist.add(path);
            return this;
        }

    }


}
