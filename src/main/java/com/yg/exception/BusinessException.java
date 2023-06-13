package com.yg.exception;

import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.helpers.MessageFormatter;

@Data
@Accessors(chain = true)
public class BusinessException extends RuntimeException {

  private String code = "-1";

  private String msg;

  private Object data;

  public BusinessException() {
    super();
  }

  public BusinessException(String msg) {
    super(msg);
    this.msg = msg;
  }

  public BusinessException(Object data) {
    super();
    this.data = data;
  }

  public BusinessException(String msg, Object data) {
    super(msg);
    this.msg = msg;
    this.data = data;
  }

  public BusinessException(String msg, Throwable e) {
    super(msg, e);
    this.msg = msg;
  }

  public BusinessException(Throwable e, Object data) {
    super(e);
    this.msg = e.getMessage();
    this.data = data;
  }

  public BusinessException(String msg, Throwable e, Object info) {
    super(msg, e);
    this.msg = msg;
    this.data = info;
  }

  public static BusinessException throwException(String msgPattern, Object... obj) {
    String message = MessageFormatter.arrayFormat(msgPattern, obj).getMessage();
    return new BusinessException(message);
  }
}
