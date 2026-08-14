package school.hei.demo.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.entity.JUser;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {
  boolean existsByReference(String reference);

  boolean existsByEmailIgnoreCase(String email);

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
