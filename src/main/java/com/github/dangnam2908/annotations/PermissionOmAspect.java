package com.github.dangnam2908.annotations;

import com.github.dangnam2908.exceptions.ExceptionOm;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PermissionOmAspect {

  private static String extractAuthorizationHeaderValue(String headers) {
    String authorizationRegex = "authorization=Bearer\\s*([^,}]+)";
    Pattern pattern = Pattern.compile(authorizationRegex);
    Matcher matcher = pattern.matcher(headers);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return null; // Authorization header not found or does not match the expected pattern
    }
  }

  @Before("@annotation(com.github.dangnam2908.annotations.PermissionOm)")
  public void process(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    PermissionOm om = method.getAnnotation(PermissionOm.class);

    Object[] args = joinPoint.getArgs();
    String authToken = null;
    for (Object arg : args) {
      authToken = extractAuthorizationHeaderValue(arg.toString().replaceAll(" ", ""));
      if (authToken != null) break;
    }
    if (authToken == null) {
      throw new ExceptionOm(HttpStatus.BAD_REQUEST, "Header required authorization");
    }
  }
}
