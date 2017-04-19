package com.sample.servlet;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletException;
import java.util.concurrent.Executors;

/**
 * @author Zero
 *         Created on 2017/2/2.
 */
public class ServletApplication {
    public static void main(String[] args) throws ServletException {
        DeploymentInfo app = Servlets.deployment()
                .setClassLoader(ServletApplication.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("app.war")
                .setUrlEncoding("utf-8")
                .setDefaultEncoding("utf-8")
                //每个应用单独设置
//                .setExecutor(Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 2)))//Servlet 默认执行线程
                //如果没有设置,那么asyncContext会使用setExecutor()中的线程执行
//                .setAsyncExecutor(Executors.newCachedThreadPool())//Servlet asyncContext.start(()->{}) 执行线程
//                .addServlet(Servlets.servlet(TextCountServlet.class).addMapping("/text_wc"))
                .addServlets(
                        Servlets.servlet(TestServlet.class).addMapping("/test"),
                        Servlets.servlet(TestAsyncServlet.class).addMapping("/testAsync").setAsyncSupported(true)
                );

        //////////////
        DeploymentInfo admin = Servlets.deployment()
                .setClassLoader(ServletApplication.class.getClassLoader())
                .setContextPath("/admin")
                .setDeploymentName("admin.war")
                .setUrlEncoding("utf-8")
                .setDefaultEncoding("utf-8")
//                .setExecutor(Executors.newFixedThreadPool(5))
                .addServlets(
                        Servlets.servlet(TestServlet.class).addMapping("/test"),
                        Servlets.servlet(TestAsyncServlet.class).addMapping("/testAsync").setAsyncSupported(true)
                );

        /////////////
        DeploymentManager appManager = Servlets.defaultContainer().addDeployment(app);
        appManager.deploy();
        HttpHandler appHandler = appManager.start();
//        PathHandler path = Handlers.path().addPrefixPath("/", appHandler);
//       这里为了测试线程模型
        PathHandler path = Handlers.path().addPrefixPath("/", new PathHandler(h -> {
            System.out.println("----->" + Thread.currentThread().getName());//XNIO-1 I/O-2
            appHandler.handleRequest(h);
        }));

        DeploymentManager adminManager = Servlets.defaultContainer().addDeployment(admin);
        adminManager.deploy();
        HttpHandler adminHandler = adminManager.start();
        path = path.addPrefixPath("/admin", adminHandler);


        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .setIoThreads(4)
                .setWorkerThreads(5)//DeploymentInfo.setExecutor()如果没有设置,则使用该设置生成执行线程池
                .build();

        server.start();
        server.getWorker().submit(() -> {
            System.out.println("First Task: " + Thread.currentThread().getName());
        });
    }
}
