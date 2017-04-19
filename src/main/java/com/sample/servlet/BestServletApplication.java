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
public class BestServletApplication {
    public static void main(String[] args) throws ServletException {
        DeploymentInfo app = Servlets.deployment()
                .setClassLoader(BestServletApplication.class.getClassLoader())
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
                        Servlets.servlet(TestServlet.class).addMapping("/text"),
                        Servlets.servlet(TestAsyncServlet.class).addMapping("/testAsync").setAsyncSupported(true)
                );

        DeploymentManager appManager = Servlets.defaultContainer().addDeployment(app);
        appManager.deploy();
        HttpHandler appHandler = appManager.start();
        PathHandler path = Handlers.path().addPrefixPath("/", appHandler);


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
