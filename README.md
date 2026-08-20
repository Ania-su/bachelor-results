# Bachelor Results — School Management API

Role-based school grading API (Spring Boot 3.2.2, Java 21, PostgreSQL). The OpenAPI contract is the source of truth: [`doc/api.yaml`](doc/api.yaml).

## 1. Stack

| Concern | Choice |
|---------|--------|
| Language / runtime | Java 21, Gradle |
| Framework | Spring Boot Web + Data JPA + Validation + Security, Thymeleaf |
| Database | PostgreSQL, Flyway (`src/main/resources/db/migration/`), Hibernate |
| Auth | Spring Security filter chain + JWT (`jjwt 0.13.0`, HMAC, 1h lifetime) |
| File / report | Apache POI (Excel graduates export), OpenHTMLToPDF (PDF transcripts), S3/SQS/EventBridge/SES |
| Other | Lombok, MapStruct-style mappers, Testcontainers, JaCoCo |

## 2. Project structure

```
.
├── doc/api.yaml                      # OpenAPI 3.1 — all endpoints, x-roles, schemas
├── build.gradle / settings.gradle
├── src/main/java/school/hei/demo/
│   ├── PojaApplication.java / handler/LambdaHandler.java
│   ├── config/SecurityConfig.java    # coarse RBAC (see §4)
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtService.java
│   │   ├── CustomUserDetails.java / CustomUserDetailsService.java
│   │   └── …
│   ├── service/                      # fine-grained auth + business logic
│   │   ├── CurrentUserService.java
│   │   ├── AuthService.java
│   │   ├── UserService.java / SpecialtyService.java / CourseService.java / GroupService.java
│   │   ├── CourseAssignmentService.java
│   │   ├── ExamService.java / ExamGroupService.java
│   │   ├── GradeService.java
│   │   ├── StudentCourseGradeService.java / StudentTranscriptService.java
│   │   ├── PromotionService.java / GraduateComputationService.java / PromotionExcelService.java
│   │   ├── StudentGroupHistoryService.java
│   │   └── event/*Service.java
│   ├── endpoint/
│   │   ├── rest/controller/          # 11 REST controllers + 5 health controllers
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java / SpecialtyController.java / CourseController.java
│   │   │   ├── CourseAssignmentController.java
│   │   │   ├── ExamController.java / ExamGroupController.java / GradeController.java
│   │   │   ├── StudentCourseGradeController.java
│   │   │   ├── GroupController.java / PromotionController.java / TranscriptController.java
│   │   │   └── health/ (Ping, HealthDb, HealthBucket, HealthEvent, HealthEmail)
│   │   ├── web/LoginViewController.java + PromotionViewController.java  # Thymeleaf views
│   │   └── event/ (EventProducer, EventConsumer, EventConf)
│   ├── domain/
│   │   ├── dto/request/  (LoginRequest, UserCreate/Update, CourseRequest, …)
│   │   ├── dto/response/ (AuthResponse, UserPage, CoursePage, GradePage, …)
│   │   └── mappers/ (UserMapper, CourseMapper, ExamMapper, …)
│   ├── entity/          # domain POJOs (User, Course, Exam, Grade, …)
│   ├── repository/      # Spring Data JPA (UserRepository, ExamRepository, …) + J* JPA entities
│   ├── enums/           # UserRole {STUDENT,TEACHER,ADMIN}, CodeType {NONE,EL,TN}
│   ├── exception/       # NotFound, Forbidden, Unauthorized, BadRequest, Conflict, GlobalExceptionHandler
│   ├── validators/      # UserValidator, ExamValidator, GradeValidator, PaginationValidator, …
│   ├── file/bucket/, file/hash/, mail/, concurrency/
│   └── endpoint/EndpointConf.java + RequestLoggerConfigurer.java
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── db/migration/V42_3__Create_tables.sql
│   │   └── templates/promotions.html
│   └── src/test/java/school/hei/demo/
│       ├── conf/ (FacadeIT, PostgresConf, TestAuthentication)
│       ├── controller/*Test.java / IT/* / service/*Test / security/JwtServiceTest.java
│       └── validators/*Test.java
```

Layering is strict: **Controller → Service (auth helpers + validator) → Repository (JPA `J*` entity) → Entity (domain POJO)**. Mappers translate between layers. `SecurityFilterChain` fronts everything.

## 3. API contract

`doc/api.yaml` is OpenAPI 3.1.0, served at `/api/v1` (local) / `https://api.example.com/v1` (prod). Every operation declares `x-roles` and `security: [{ bearerAuth: [] }]`. The file currently documents **30 paths** (including 4 web view paths: `GET /web/login`, `POST /web/login`, `GET /web/promotions`, `GET /web/promotions/{academicYear}/download` returning `text/html` / `302`). Health endpoints (`/ping`, `/health/*`) are intentionally omitted.

## 4. How routes are protected

Protection is two layers. No `@PreAuthorize` annotations — everything is explicit and testable in the service layer.

### 4.1 Layer 1 — coarse RBAC in `config/SecurityConfig.java:21`

Stateless, CSRF/formLogin/httpBasic disabled, `SessionCreationPolicy.STATELESS`, `JwtAuthenticationFilter` inserted before `UsernamePasswordAuthenticationFilter`:

```java
authorize
  .requestMatchers("/auth/login").permitAll()
  .requestMatchers(GET, "/users").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/users/*").hasAnyRole("ADMIN","TEACHER","STUDENT")
  .requestMatchers("/users/**").hasRole("ADMIN")
  .requestMatchers(GET, "/specialties","/specialties/**").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/courses/*/assignments","/courses/*/assignments/**").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/courses/*/exams/*/grades","/courses/*/exams/*/grades/**").hasAnyRole("ADMIN","TEACHER","STUDENT")
  .requestMatchers("/courses/*/exams/*/grades","/courses/*/exams/*/grades/**").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/courses/*/exams/*/groups","/courses/*/exams/*/groups/**").hasAnyRole("ADMIN","TEACHER","STUDENT")
  .requestMatchers("/courses/*/exams/*/groups","/courses/*/exams/*/groups/**").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/courses/*/exams","/courses/*/exams/**").hasAnyRole("ADMIN","TEACHER","STUDENT")
  .requestMatchers("/courses/*/exams","/courses/*/exams/**").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/courses","/courses/**").hasAnyRole("ADMIN","TEACHER","STUDENT")
  .requestMatchers(GET, "/groups","/groups/**").hasAnyRole("ADMIN","TEACHER")
  .requestMatchers(GET, "/me/transcript-email").hasRole("STUDENT")
  .requestMatchers(GET, "/students/*/transcript").hasRole("ADMIN")
  .anyRequest().hasRole("ADMIN")   // promotions, web/promotions, /courses/{id}/student-grade, POST/PATCH/DELETE on specialties/courses/groups, …
```

Key point: `GET` is more permissive than writes, and `anyRequest().hasRole("ADMIN")` locks down anything not explicitly listed.

### 4.2 JWT — `security/JwtService.java:24` + `security/JwtAuthenticationFilter.java:21`

* Login: `POST /auth/login` → `service/AuthService.java:login` verifies `passwordEncoder.matches`, calls `JwtService.generateToken(userId, email, role)` (`subject=userId`, claims `email,role`, `issuedAt`/`expiration` +1h, `Keys.hmacShaKeyFor(jwtSecret)`). Returns `{accessToken, tokenType:"Bearer", expiresIn, user}` and sets an `HttpOnly` cookie `session`.
* On every request `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) resolves the token from `Authorization: Bearer <token>` **or** `Cookie: session=<token>` (`JwtAuthenticationFilter.java:57`), checks `jwtService.isTokenValid` (`Jwts.parser().verifyWith(key).parseSignedClaims`), loads `CustomUserDetailsService.loadUserById(subject)` → `UsernamePasswordAuthenticationToken` with authority `ROLE_<UserRole>` (`security/CustomUserDetails.java:19`) → `SecurityContextHolder`.
* `service/CurrentUserService.java:12` is the single accessor: extracts `CustomUserDetails.getUser()` or throws `UnauthorizedException` (→ 401).

### 4.3 Error mapping — `exception/GlobalExceptionHandler.java:12`

`UnauthorizedException→401`, `ForbiddenException→403`, `NotFoundException→404`, `BadRequestException→400`, `ConflictException→409`, `MethodArgumentTypeMismatchException→400` (invalid UUID), `HttpMessageNotReadableException→400`, `DataIntegrityViolationException→409`.

## 5. Fine-grained helpers — which professor can do what, which student can do what

Controllers are thin (they only delegate). Each service contains small private `ensure*` / `is*` helpers that read `CurrentUserService.getCurrentUser()` and enforce ownership / assignment. All failures throw `ForbiddenException` (403) or `NotFoundException` (404 — also prevents ID enumeration).

| Service | Helpers | What they enforce |
|---------|---------|-------------------|
| `service/UserService.java:94` | inline check in `findById` | `STUDENT` may `GET /users/{userId}` **only** for its own `id` (`:99`); otherwise `ForbiddenException`. `ensureSpecialtyExists` (`:169`) on create/update. |
| `service/CourseAssignmentService.java:78` | `ensureTeacherAssignedToCourse` (`:78`) → `isUserAssignedToCourse` (`:86`, `existsByCourse_IdAndTeacherId`); `ensureBelongsToCourse` (`:72`) | `TEACHER` may list/get assignments **only** for courses it is assigned to (via `CourseAssignment`). Child-not-in-parent → `NotFoundException`. `ADMIN` bypasses the teacher check. |
| `service/ExamService.java:105` | `ensureTeacherAssigned` (`:105`); `isStudentAllowedOnCourse` (`:112`) → specialty lookup + `courseSpecialtyRepository.findByCourseId`; `isAllowedSpecialty` (`:131`, `NONE` or matching `EL`/`TN`); `entryYearStart` (`:136`); `ensureBelongsToCourse` (`:143`) | `TEACHER` must be assigned to the course. `STUDENT` must have `specialtyId`/`entryYear` and the course's `CodeType` must allow it; `listForCourse` returns `[]` if not allowed, `get` throws `ForbiddenException`; date filter `exam.dateExam >= entryYearStart`. |
| `service/ExamGroupService.java:24` | delegation `examService.get(courseId, examId)` (`:25`) + `ensureBelongsToExam` (`:56`) | Inherits the entire `ExamService` gate — no duplicated logic. If the exam is not accessible, the exam-group is not accessible either. |
| `service/GradeService.java:173` | `ensureReadAccess` (`:173`) → `ensureTeacherAssigned`; `ensureWriteAccess` (`:179`, rejects `STUDENT` with "read-only"); `ensureTeacherAssigned` (`:187`); `ensureExamBelongsToCourse` (`:194`); `ensureGradeBelongsToExam` (`:206`) | Read: `STUDENT` forced to `studentId = currentUser.id` (`:45`) and may `GET /courses/.../grades` / `GET /grades/{gradeId}` only for its own grades (`:46`, `:85`). Write (`POST/PATCH/DELETE`): `STUDENT` always `ForbiddenException`; `TEACHER` only if assigned to the course. Hierarchy `grade→exam→course` validated at every level. |
| `service/StudentCourseGradeService.java:35` | (no role gate — route itself is `anyRequest→ADMIN`, `doc/api.yaml:335` documents `x-roles: [ADMIN]`) | Validates that both user and course exist; computes weighted average over all exams of the course. |
| `StudentTranscriptService` / `PromotionService` | protected by `SecurityConfig` matchers (`/me/transcript-email→STUDENT`, `/students/*/transcript→ADMIN`, promotions `anyRequest→ADMIN`) + `CurrentUserService` for the `/me` variant | — |

Idiom in one line: `ADMIN` bypasses teacher checks; `TEACHER` is gated by `course_assignment`; `STUDENT` is gated by identity (`id` equality), specialty/code-type, and date.

## 6. Nested routes — a deliberate design choice

The project **tries to have as many nested routes as possible** so each controller owns a narrow slice and the hierarchy itself documents the domain and helps authorization.

Flat alternative vs what this codebase does:

```
# flat (one controller would handle everything)
GET /examGroups?examId=…&courseId=…
POST /grades { courseId, examId, studentId, value }

# nested (this project — hierarchical, self-validating)
GET  /courses/{courseId}/exams
GET  /courses/{courseId}/exams/{examId}
GET  /courses/{courseId}/exams/{examId}/groups
GET  /courses/{courseId}/exams/{examId}/groups/{examGroupId}
GET  /courses/{courseId}/exams/{examId}/grades
PATCH /courses/{courseId}/exams/{examId}/grades/{gradeId}
GET  /courses/{courseId}/assignments/{assignmentId}
GET  /courses/{courseId}/student-grade/{student-id}
```

Result: **14 resource controllers** instead of a few fat ones:

* `AuthController: /auth`, `UserController: /users`, `SpecialtyController: /specialties`, `CourseController: /courses`, `CourseAssignmentController: /courses/{courseId}/assignments`, `ExamController: /courses/{courseId}/exams`, `ExamGroupController: /courses/{courseId}/exams/{examId}/groups`, `GradeController: /courses/{courseId}/exams/{examId}/grades`, `StudentCourseGradeController: /courses/{courseId}/student-grade`, `GroupController: /groups` (+ `/groups/{groupId}/students`), `PromotionController: /promotions`, `TranscriptController: /students/{id}/transcript + /me/transcript-email`, `LoginViewController: /web/login`, `PromotionViewController: /web/promotions`.

Benefits:

* **Readability** — a controller's `@RequestMapping` tells you its aggregate at a glance (`endpoint/rest/controller/GradeController.java:13` → `/courses/{courseId}/exams/{examId}/grades`).
* **Authorization reuse** — child controllers delegate to the parent's `ensureBelongsTo*` / `ensureTeacherAssigned` (e.g. `ExamGroupService.listForExam` calls `ExamService.get`); impossible to access a grade without first passing the course-level gate.
* **ID-enumeration safety** — a mismatched parent `courseId` returns `NotFoundException` rather than leaking the child's existence.
* **OpenAPI clarity** — each path group maps to a tag (`Authentication`, `Users`, `Courses`, `Exams`, `Exam groups`, `Grades`, `Groups`, `Promotions`, `Web`).

## 7. Running and testing

```bash
./gradlew test                         # Testcontainers spins up Postgres
./gradlew bootRun                      # requires SPRING_DATASOURCE_*, SPRING_JWT_SECRET
./gradlew jacocoTestReport             # HTML report at build/reports/jacoco/
```

Environment: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JWT_SECRET` (HMAC key), AWS keys for S3/SQS/SES if exercising promotions/transcripts.

## 8. Full route table (from `doc/api.yaml`)

| Method | Path | Controller | `x-roles` |
|--------|------|------------|-----------|
| POST | `/auth/login` | `AuthController.java` | — (permitAll) |
| GET/POST | `/users` | `UserController.java` | `[ADMIN,TEACHER]` / `[ADMIN]` |
| GET/PATCH/DELETE | `/users/{userId}` | `UserController.java` | `[ADMIN,TEACHER,STUDENT]*` / `[ADMIN]` |
| GET/POST | `/specialties` | `SpecialtyController.java` | `[ADMIN,TEACHER]` / `[ADMIN]` |
| GET/PATCH/DELETE | `/specialties/{specialtyId}` | `SpecialtyController.java` | `[ADMIN,TEACHER]` / `[ADMIN]` |
| GET/POST | `/courses` | `CourseController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN]` |
| GET/PATCH/DELETE | `/courses/{courseId}` | `CourseController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN]` |
| GET/POST | `/courses/{courseId}/assignments` | `CourseAssignmentController.java` | `[ADMIN,TEACHER]` / `[ADMIN]` |
| GET/PATCH/DELETE | `/courses/{courseId}/assignments/{assignmentId}` | `CourseAssignmentController.java` | `[ADMIN,TEACHER]` / `[ADMIN]` |
| GET | `/courses/{courseId}/student-grade/{student-id}` | `StudentCourseGradeController.java` | `[ADMIN]` |
| GET/POST | `/courses/{courseId}/exams` | `ExamController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN,TEACHER]` |
| GET/PATCH/DELETE | `/courses/{courseId}/exams/{examId}` | `ExamController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN,TEACHER]` |
| GET/POST | `/courses/{courseId}/exams/{examId}/groups` | `ExamGroupController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN,TEACHER]` |
| GET/DELETE | `/courses/{courseId}/exams/{examId}/groups/{examGroupId}` | `ExamGroupController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN,TEACHER]` |
| GET/POST | `/courses/{courseId}/exams/{examId}/grades` | `GradeController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN,TEACHER]` |
| GET | `/courses/{courseId}/exams/{examId}/grades/history` | `GradeController.java` | `[ADMIN,TEACHER]` |
| GET/PATCH/DELETE | `/courses/{courseId}/exams/{examId}/grades/{gradeId}` | `GradeController.java` | `[ADMIN,TEACHER,STUDENT]` / `[ADMIN,TEACHER]` |
| GET/POST/PATCH/DELETE | `/groups` / `/groups/{groupId}` | `GroupController.java` | `[ADMIN,TEACHER]` / `[ADMIN]` |
| GET | `/groups/{groupId}/students` | `GroupController.java` | `[ADMIN,TEACHER]` |
| POST/DELETE | `/groups/{groupId}/students/{studentId}` | `GroupController.java` | `[ADMIN]` |
| GET | `/me/transcript-email` | `TranscriptController.java` | `[STUDENT]` |
| GET | `/students/{studentId}/transcript` | `TranscriptController.java` | `[ADMIN]` |
| GET | `/promotions` | `PromotionController.java` | `[ADMIN]` |
| GET | `/promotions/{academicYear}/graduates` | `PromotionController.java` | `[ADMIN]` |
| GET | `/promotions/{academicYear}/graduates/download` | `PromotionController.java` | `[ADMIN]` |
| GET/POST | `/web/login` | `LoginViewController.java` | — (permitAll) |
| GET | `/web/promotions` | `PromotionViewController.java` | `[ADMIN]` |
| GET | `/web/promotions/{academicYear}/download` | `PromotionViewController.java` | `[ADMIN]` (302 redirect) |
* Students on `GET /users/{userId}` and grades/exams are further restricted by the service helpers to their own data / specialty.

