package com.coma.comaroom;

import com.coma.comaroom.utils.ErrorCode;
import lombok.Getter;

public class BusinessException extends RuntimeException {
  @Getter
  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
      super(errorCode.getMessage());
      this.errorCode = errorCode;
  }
}
