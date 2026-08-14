package school.hei.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
    return build(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequestException(BadRequestException e) {
    return build(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiError> handleConflictException(ConflictException e) {
    return build(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException e) {
    return build(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    return build(
        HttpStatus.BAD_REQUEST, "Invalid id '" + e.getValue() + "': must be a valid UUID.");
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    return build(HttpStatus.BAD_REQUEST, "Request body must be valid JSON.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception e) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String message) {
    ApiError error = new ApiError(status.value(), status.getReasonPhrase(), message);
    return ResponseEntity.status(status).body(error);
  }
}
