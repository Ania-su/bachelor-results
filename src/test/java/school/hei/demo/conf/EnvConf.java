package school.hei.demo.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {
  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "SPRING_JWT_SECRET", () -> "integration-test-only-secret-key-to-change-to-32-bytes-min");
  }
}
