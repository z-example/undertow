package com.sample.servlet;

import io.undertow.Handlers;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import javax.servlet.ServletContext;

public class NonBlockingHandlerExtension implements ServletExtension {

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addInitialHandlerChainWrapper(new HandlerWrapper() {
            @Override
            public HttpHandler wrap(final HttpHandler handler) {
                 return handler;
            }
        });
    }
}