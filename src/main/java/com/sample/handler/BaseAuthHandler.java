package com.sample.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.Base64;

/**
 * @author Zero
 *         Created on 2017/2/6.
 */
public class BaseAuthHandler implements HttpHandler {
    private HttpHandler next;

    public BaseAuthHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        //浏览器收到未授权头消息时,会弹出登录对话框,然后添加授权信息(用户名和密码)到header中在此请求改页面
        //登出是个问题,授权后信息是缓存在浏览器中的,之后每次请求都会带上授权信息(清除cookie是不起作用的)
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
