package com.github.dangnam2908.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
@ControllerAdvice
public class GlobalHandleException extends ResponseEntityExceptionHandler {
  SendMessageTelegram sendMessageTelegram = new SendMessageTelegram();

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponse> globalExceptionHandler(
    Exception ex, HttpServletRequest request) {
    sendNotification(ex, request);
    if (ex instanceof ExceptionOm) {
      ExceptionOm exceptionOm = (ExceptionOm) ex;
      ExceptionResponse exceptionResponse =
        new ExceptionResponse(
          exceptionOm.status,
          new Date(),
          exceptionOm.getMessage(),
          exceptionOm.messageCode,
          exceptionOm.getMessage(),
          request.getServletPath());
      return new ResponseEntity<>(exceptionResponse, exceptionOm.status);
    }
    ExceptionResponse exceptionResponse =
      new ExceptionResponse(
        INTERNAL_SERVER_ERROR,
        new Date(),
        "Đã có lỗi xảy ra.",
        "INTERNAL_SERVER_ERROR",
        ex.getMessage(),
        request.getServletPath());
    return new ResponseEntity<>(exceptionResponse, INTERNAL_SERVER_ERROR);
  }

  private void sendNotification(Exception ex, HttpServletRequest request) {
    try {
      String logLevel = "ERROR";
      String msg = "";

      if (ex instanceof ExceptionOm) {
        ExceptionOm exceptionOm = (ExceptionOm) ex;
        if (exceptionOm.status.value() < 500) {
          msg += "<b>WARNING</b> : " + ex.getMessage() + "(" + exceptionOm.status.value() + ") \n";
        } else {
          msg += "<b>ERROR</b> : " + ex.getMessage() + "(" + exceptionOm.status.value() + ") \n";
        }
      } else {
        msg += "<b>ERROR</b> : " + ex.getMessage() + " \n";
      }
      try {
        msg += "<b>URI</b> : " + request.getRequestURI();
      } catch (Exception ignored) {
      }
      sendMessageTelegram.send(msg);
    } catch (Exception ignore) {
    }
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ValidDetails validDetails = new ValidDetails();
    Map<String, String> message = new HashMap<>();
    if (ex instanceof MethodArgumentNotValidException) {
      List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
      for (FieldError fieldError : fieldErrors) {
        message.put(fieldError.getField(), fieldError.getDefaultMessage());
      }
      validDetails.setMessage(message);
    } else {
      message.put("default", ex.getLocalizedMessage());
      validDetails.setMessage(message);
    }
    validDetails.setStatus(HttpStatus.BAD_REQUEST.value());
    validDetails.setTimestamp(new Date());
    validDetails.setError("Not valid exception");
    validDetails.setPath(((ServletWebRequest) request).getRequest().getServletPath());
    return new ResponseEntity(validDetails, status);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
    HttpMessageNotReadableException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ExceptionResponse exceptionResponse =
      new ExceptionResponse(
        BAD_REQUEST,
        new Date(),
        "Malformed JSON request",
        "MALFORMED_JSON_REQUEST",
        ex.getLocalizedMessage(),
        ((ServletWebRequest) request).getRequest().getServletPath());
    return new ResponseEntity<>((Object) exceptionResponse, BAD_REQUEST);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ExceptionResponse> handleException(MissingRequestHeaderException ex) {
    if (ex.getMessage().contains("x-om")) {
      ExceptionResponse errorDetails =
        new ExceptionResponse(
          HttpStatus.UNAUTHORIZED,
          new Date(),
          "unauthorized",
          "UNAUTHORIZED",
          "unauthorized",
          null);
      return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    ExceptionResponse errorDetails =
      new ExceptionResponse(
        HttpStatus.BAD_REQUEST, new Date(), ex.getMessage(), "MISSING_REQUEST_HEADER", ex.getMessage(), null);

    return new ResponseEntity<>(errorDetails, BAD_REQUEST);
  }

  public static class SendMessageTelegram {
    public void send(String msg) {

    }
  }
}
