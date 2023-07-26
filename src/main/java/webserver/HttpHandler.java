package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.annotation.Container;
import support.web.ControllerResolver;
import support.web.ModelAndView;
import support.web.ResponseEntity;
import support.web.ViewResolver;
import support.web.exception.*;
import webserver.request.HttpRequest;
import webserver.response.HttpResponse;
import webserver.response.HttpStatus;
import webserver.response.strategy.NotFound;

import java.lang.reflect.InvocationTargetException;

@Container
public class HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    public void doGet(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException {
        String path = request.getRequestPath();

        try {
            try {
                ResponseEntity responseEntity = ControllerResolver.invoke(path, request, response);
                response.setStatus(responseEntity.getStatus());
                response.appendHeader(responseEntity.getHeader());
            } catch (NotSupportedException e) {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName(path);
                callViewResolver(request, response, modelAndView);
            }
        } catch (FoundException e) {
            response.setStatus(e.getHttpStatus());
            response.appendHeader("Location", e.getRedirectionUrl());
        } catch (HttpException e) {
            logger.debug(e.getMessage());
            response.setStatus(e.getHttpStatus());
        } catch (Exception e) {
            logger.debug(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public void doPost(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException {
        String path = request.getRequestPath();

        try {
            ResponseEntity responseEntity = ControllerResolver.invoke(path, request, response);
            response.setStatus(responseEntity.getStatus());
            response.appendHeader(responseEntity.getHeader());
        } catch (NotSupportedException e) {
            buildErrorResponse(request, response);
        } catch (FoundException e) {
            response.setStatus(e.getHttpStatus());
            response.appendHeader("Location", e.getRedirectionUrl());
        } catch (HttpException e) {
            response.setStatus(e.getHttpStatus());
        } catch (Exception e) {
            logger.debug(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void callViewResolver(HttpRequest request, HttpResponse response, ModelAndView modelAndView) throws ServerErrorException {
        try {
            ViewResolver.buildView(request, response, modelAndView);
            response.setStatus(HttpStatus.OK);
        } catch (NotFoundException e) {
            buildErrorResponse(request, response);
        }
    }

    private void buildErrorResponse(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND);
        response.buildHeader(new NotFound());
        ViewResolver.buildErrorView(request, response);
    }

}
