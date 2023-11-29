package com.dangnam.util.utils;

import com.dangnam.util.utils.values.HttpResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class RequestUtils {
  /**
   * 5 second
   */
  private static int timeout = 5;

  public static <T> HttpResponse sendRequest(
    HttpMethod method, String requestUrl, T mData, Map<String, String> headerParam) {
    try {
      RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory(timeout));
      HttpHeaders headers = new HttpHeaders();

      if (headerParam != null) {
        for (Map.Entry<String, String> entry : headerParam.entrySet()) {
          headers.add(entry.getKey(), entry.getValue());
        }
      }
      if (headers.get("Content-Type") == null) {
        headers.add("Content-Type", "application/json");
      }
      HttpEntity<T> data = new HttpEntity<T>(mData, headers);

      ResponseEntity<String> response =
        restTemplate.exchange(requestUrl, method, data, String.class);

      return new HttpResponse(response.getStatusCode(), response.getBody(), response.getHeaders());
    } catch (HttpClientErrorException hex) {
      return new HttpResponse(
        hex.getStatusCode(), hex.getResponseBodyAsString(), hex.getResponseHeaders());
    } catch (Exception e) {
      throw new RuntimeException("Restemplace err: {}", e);
    }
  }

  private static SimpleClientHttpRequestFactory getClientHttpRequestFactory(int timeOut) {
    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    // Connect timeout
    clientHttpRequestFactory.setConnectTimeout(timeOut * 1000);

    // Read timeout
    clientHttpRequestFactory.setReadTimeout(timeOut * 1000);
    return clientHttpRequestFactory;
  }
}
