package school.hei.demo.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.model.JUser;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {
  boolean existsByReference(String reference);

  boolean existsByReferenceAndIdNot(String reference, UUID id);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

  java.util.Optional<JUser> findByEmailIgnoreCase(String email);

  @Query(
      """
      select u from JUser u
      where u.userRole = coalesce(:role, u.userRole)
        and lower(u.reference) like lower(concat('%', coalesce(:reference, ''), '%'))
        and lower(u.firstName) like lower(concat('%', coalesce(:firstName, ''), '%'))
        and lower(u.lastName) like lower(concat('%', coalesce(:lastName, ''), '%'))
        and lower(u.email) like lower(concat('%', coalesce(:email, ''), '%'))
      """)
  Page<JUser> findAllByFilters(
      @Param("role") UserRole role,
      @Param("reference") String reference,
      @Param("firstName") String firstName,
      @Param("lastName") String lastName,
      @Param("email") String email,
      Pageable pageable);
}
