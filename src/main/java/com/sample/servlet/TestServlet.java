package com.sample.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Zero
 *         Created on 2017/2/22.
 */
@WebServlet(name = "TestServlet")
public class TestServlet extends HttpServlet {
    private int count;//非线程安全,所有请求共享的变量
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("TestServlet: " + Thread.currentThread().getName());
        response.setContentType("text/html");
        response.getWriter().println(++count);
    }
}
