package school.hei.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.demo.repository.entity.JUser;

@Repository
public interface UserRepository extends JpaRepository<JUser, java.util.UUID> {
  boolean existsByReference(String reference);

  boolean existsByEmailIgnoreCase(String email);
}
