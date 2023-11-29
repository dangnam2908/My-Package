package com.dangnam.util.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashUtil {

  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String HMAC_SHA512 = "HmacSHA512";

  public static String getMD5(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] messageDigest = md.digest(input.getBytes());

      BigInteger number = new BigInteger(1, messageDigest);
      String hashtext = number.toString(16);
      while (hashtext.length() < 32) {
        hashtext = "0" + hashtext;
      }
      return hashtext;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("resource")
  private static String toHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    Formatter formatter = new Formatter(sb);
    for (byte b : bytes) {
      formatter.format("%02x", b);
    }
    return sb.toString();
  }

  public static String signHmacSHA256(String data, String secretKey)
    throws NoSuchAlgorithmException, InvalidKeyException {
    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA256);
    Mac mac = Mac.getInstance(HMAC_SHA256);
    mac.init(secretKeySpec);
    byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return toHexString(rawHmac);
  }

  public static String signHmacSHA512(String value, String secret) {
    String result;
    try {
      Mac hmacSHA512 = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
      hmacSHA512.init(secretKeySpec);

      byte[] digest = hmacSHA512.doFinal(value.getBytes());
      BigInteger hash = new BigInteger(1, digest);
      result = hash.toString(16);
      if ((result.length() % 2) != 0) {
        result = "0" + result;
      }
    } catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException ex) {
      throw new RuntimeException("Problemas calculando HMAC", ex);
    }
    return result;
  }
}
