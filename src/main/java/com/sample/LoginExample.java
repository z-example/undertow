package com.sample;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.util.Cookies;

import java.security.Principal;
import java.util.Set;

/**
 * @author Zero
 *         Created on 2017/2/4.
 */
public class LoginExample {
    public static void main(String[] args) {
        RoutingHandler router = Handlers.routing();


        router.get("/login", exchange -> {
            boolean b = exchange.getSecurityContext().login("admin", "123456");
            exchange.getResponseCookies().put("sid", new CookieImpl("sid", "sss"));
            exchange.getResponseCookies().put("token", new CookieImpl("token", "role:admin"));
            System.out.println(b);
        });

        router.get("/admin/*", exchange -> {
            exchange.getResponseSender().send(exchange.getSecurityContext().getAuthenticatedAccount().toString());
        });

        //HttpHandler链
        HttpHandler handler = new MetricsHandler(router);//router是MetricsHandler的next handler
        handler = new SimpleErrorPageHandler(handler);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, new MyIdentityManager(), handler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(handler)
                .setIoThreads(4)
                .build();
        server.start();
    }

    static class MyIdentityManager implements IdentityManager {

        @Override
        public Account verify(Account account) {
            return null;
        }

        @Override
        public Account verify(String id, Credential credential) {
            if ("admin".equals(id)) {
                return new MyAccount("admin");
            }
            return null;
        }

        @Override
        public Account verify(Credential credential) {
            return null;
        }
    }

    static class MyAccount implements Account {

        private String username;

        public MyAccount(String username) {
            this.username = username;
        }

        @Override
        public Principal getPrincipal() {
            return new UserPrincipal(username);
        }

        @Override
        public Set<String> getRoles() {
            return null;
        }
    }

    static class UserPrincipal implements Principal {
        private String username;

        public UserPrincipal(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }
    }
}
