package com.sampoom.backend.common.exception;

import com.sampoom.backend.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
  public ForbiddenException() {
    super(HttpStatus.FORBIDDEN);
  }

  public ForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }

  public ForbiddenException(ErrorStatus errorStatus) {
      super(errorStatus.getHttpStatus(), errorStatus.getMessage(), errorStatus.getCode());
  }
}
