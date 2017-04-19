package com.sample.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Zero
 *         Created on 2017/2/22.
 */
@WebServlet(name = "TestServlet")
public class TestAsyncServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /*

    --------下面是输出内容
    startAsync before: XNIO-1 task-1
    startAsync after: XNIO-1 task-2
    --------
    结论:Undertow默认就是开启了异步的(Tomcat9默认也是异步),
   */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");//必须设置ContentType,否则会变为同步效果,不知是不是Undertow的bug
        PrintWriter out = response.getWriter();
        out.println("Start At : "+ new Date()+" ,Thread : " + Thread.currentThread().getName()+"</br>");
        out.flush();
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);//默认30s
        //启动异步调用的线程
        asyncContext.start(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                out.println("Async response At : "+ new Date()+" ,Thread : " + Thread.currentThread().getName()+"</br>");
//                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            asyncContext.complete();//必须
        });
        out.println("End At："+new Date()+".<br/>");
        out.flush();
    }
}
