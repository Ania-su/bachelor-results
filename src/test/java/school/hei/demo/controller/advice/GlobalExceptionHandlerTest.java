package school.hei.demo.controller.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import school.hei.demo.exception.ApiError;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.exception.ForbiddenException;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldHandleKnownExceptions() {
    assertEquals(HttpStatus.BAD_REQUEST.value(), body(handler.handleBadRequestException(new BadRequestException("bad"))).status());
    assertEquals(HttpStatus.CONFLICT.value(), body(handler.handleConflictException(new ConflictException("conflict"))).status());
    assertEquals(HttpStatus.NOT_FOUND.value(), body(handler.handleNotFound(new NotFoundException("not found"))).status());
    assertEquals(HttpStatus.FORBIDDEN.value(), body(handler.handleForbiddenException(new ForbiddenException("forbidden"))).status());
  }

  @Test
  void shouldHandleUnreadableRequestBody() {
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("invalid", new MockHttpInputMessage(new byte[0]));

    ApiError error = body(handler.handleHttpMessageNotReadableException(exception));

    assertEquals(HttpStatus.BAD_REQUEST.value(), error.status());
  }

  private ApiError body(org.springframework.http.ResponseEntity<ApiError> response) {
    return response.getBody();
  }
}
