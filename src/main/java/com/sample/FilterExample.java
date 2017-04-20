package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class FilterExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();

        router.get("/login", exchange -> {
            exchange.getResponseSender().send("Login page");
        });

        router.get("/admin", exchange -> {
            exchange.getResponseSender().send(exchange.getRelativePath());
        });
//        router.get("/static",Handlers.resource(new PathResourceManager()));

        //HttpHandler链
        HttpHandler handler = new SimpleErrorPageHandler(new LoginFilter(router));
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(1)
                .build();
        server.start();
    }

    private static class LoginFilter implements HttpHandler {

        private HttpHandler next;


        public LoginFilter(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            String path = exchange.getRequestPath();
            if (path.equals("/login")||path.startsWith("/static/") || (exchange.getSecurityContext() != null && exchange.getSecurityContext().isAuthenticated())) {
                next.handleRequest(exchange);
            } else {
                exchange.setStatusCode(403);//设置错误状态码,具体处理交给SimpleErrorPageHandler统一实现
            }
        }
    }

/*

    private static abstract class FilterHandler implements HttpHandler {
        private HttpHandler next;

        public FilterHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            if (doFilter(exchange)) {
                next.handleRequest(exchange);
            }
        }

        protected boolean doFilter(HttpServerExchange exchange) {
            return true;
        }

    }
*/


}
