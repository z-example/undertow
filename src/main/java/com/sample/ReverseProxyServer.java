package com.sample;

import io.undertow.Undertow;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.Headers;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 反向代理的例子
 * <p>
 * https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/reverseproxy/ReverseProxyServer.java
 *
 * @author Stuart Douglas
 */
public class ReverseProxyServer {

    public static void main(final String[] args) {
        try {
            final Undertow server1 = Undertow.builder()
                    .addHttpListener(8081, "localhost")
                    .setHandler(exchange -> {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Server1");
                    })
                    .build();

            server1.start();

            final Undertow server2 = Undertow.builder()
                    .addHttpListener(8082, "localhost")
                    .setHandler(exchange -> {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Server2");
                    })
                    .build();
            server2.start();

            final Undertow server3 = Undertow.builder()
                    .addHttpListener(8083, "localhost")
                    .setHandler(exchange -> {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Server3");
                    })
                    .build();

            server3.start();

            //JvmRoute就是在这里使用的,根据用户session绑定到某台server中
            //Tomcat也有JvmRoute
            LoadBalancingProxyClient loadBalancer = new LoadBalancingProxyClient()
                    .addHost(new URI("http://localhost:8081"))
                    .addHost(new URI("http://localhost:8082"))
                    .addHost(new URI("http://localhost:8083"))
                    .setConnectionsPerThread(20);

            Undertow reverseProxy = Undertow.builder()
                    .addHttpListener(8080, "0.0.0.0")
                    .setIoThreads(4)
                    .setHandler(new ProxyHandler(loadBalancer, 30000, ResponseCodeHandler.HANDLE_404))
                    .build();
            reverseProxy.start();

            //

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}