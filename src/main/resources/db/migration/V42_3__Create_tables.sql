CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ------------------------------------------------------------
-- enum types
-- ------------------------------------------------------------

CREATE TYPE code_type AS ENUM ('NONE', 'EL', 'TN');
CREATE TYPE user_role AS ENUM ('STUDENT', 'TEACHER', 'ADMIN');

-- ------------------------------------------------------------
-- specialty
-- ------------------------------------------------------------

CREATE TABLE specialty (
                           id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           code  code_type NOT NULL UNIQUE,
                           label VARCHAR(255) NOT NULL
);

-- ------------------------------------------------------------
-- course
-- ------------------------------------------------------------

CREATE TABLE course (
                        id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        reference VARCHAR(8) NOT NULL UNIQUE,
                        title     VARCHAR(255) NOT NULL,
                        semester  INT NOT NULL,
                        credits   INT NOT NULL,
                        CONSTRAINT course_semester_check CHECK (semester BETWEEN 1 AND 6),
                        CONSTRAINT course_credits_check CHECK (credits > 0)
);

-- ------------------------------------------------------------
-- course_specialty
-- ------------------------------------------------------------

CREATE TABLE course_specialty (
                                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  id_course    UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
                                  id_specialty UUID NOT NULL REFERENCES specialty(id) ON DELETE CASCADE,
                                  CONSTRAINT course_specialty_key UNIQUE (id_course, id_specialty)
);

CREATE INDEX idx_course_specialty_specialty ON course_specialty(id_specialty);

-- ------------------------------------------------------------
-- groups
-- ------------------------------------------------------------

CREATE TABLE groups (
                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        reference     CHAR(2) NOT NULL,
                        academic_year INT NOT NULL,
                        CONSTRAINT groups_reference_year_key UNIQUE (reference, academic_year)
);

-- ------------------------------------------------------------
-- users
-- ------------------------------------------------------------

CREATE TABLE users (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       reference     VARCHAR(8) NOT NULL UNIQUE,
                       first_name    VARCHAR(255) NOT NULL,
                       last_name     VARCHAR(255) NOT NULL,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       user_role     user_role NOT NULL,
                       id_specialty  UUID NOT NULL REFERENCES specialty(id) ON DELETE RESTRICT,
                       entry_year    INT,
                       created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_role ON users(user_role);
CREATE INDEX idx_users_specialty ON users(id_specialty);

-- ------------------------------------------------------------
-- student_group_history
-- ------------------------------------------------------------

CREATE TABLE student_group_history (
                                       id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       id_student UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       id_group   UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                                       start_date DATE NOT NULL,
                                       end_date   DATE,
                                       CONSTRAINT student_group_history_dates_check CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_sgh_student ON student_group_history(id_student);
CREATE INDEX idx_sgh_group ON student_group_history(id_group);

CREATE UNIQUE INDEX student_group_history_current_group_idx
    ON student_group_history(id_student)
    WHERE end_date IS NULL;

CREATE TABLE course_assignment (
                                   id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   id_course  UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
                                   id_teacher UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                                   id_group   UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                                   CONSTRAINT course_assignment_key UNIQUE (id_course, id_teacher, id_group)
);

CREATE INDEX idx_course_assignment_course ON course_assignment(id_course);
CREATE INDEX idx_course_assignment_teacher ON course_assignment(id_teacher);
CREATE INDEX idx_course_assignment_group ON course_assignment(id_group);

-- ------------------------------------------------------------
-- exam
-- ------------------------------------------------------------

CREATE TABLE exam (
                      id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      id_course  UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
                      date_exam  TIMESTAMP NOT NULL,
                      coef       NUMERIC(3,2) NOT NULL,
                      CONSTRAINT exam_coef_check CHECK (coef > 0 AND coef <= 1)
);

CREATE INDEX idx_exam_course ON exam(id_course);

-- ------------------------------------------------------------
-- exam_group
-- ------------------------------------------------------------

CREATE TABLE exam_group (
                            id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            id_exam  UUID NOT NULL REFERENCES exam(id) ON DELETE CASCADE,
                            id_group UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                            CONSTRAINT exam_group_key UNIQUE (id_exam, id_group)
);

CREATE INDEX idx_exam_group_group ON exam_group(id_group);

-- ------------------------------------------------------------
-- grade
-- ------------------------------------------------------------

CREATE TABLE grade (
                       id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       id_student UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       id_exam    UUID NOT NULL REFERENCES exam(id) ON DELETE CASCADE,
                       value      NUMERIC(4,2) NOT NULL,
                       updated_at TIMESTAMP NOT NULL DEFAULT now(),
                       CONSTRAINT grade_student_exam_key UNIQUE (id_student, id_exam),
                       CONSTRAINT grade_value_check CHECK (value >= 0 AND value <= 20)
);

CREATE INDEX idx_grade_student ON grade(id_student);
CREATE INDEX idx_grade_exam ON grade(id_exam);

-- ------------------------------------------------------------
-- grade_history
-- ------------------------------------------------------------

CREATE TABLE grade_history (
                               id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               id_note    UUID NOT NULL REFERENCES grade(id) ON DELETE CASCADE,
                               old_value  NUMERIC(4,2),
                               new_value  NUMERIC(4,2) NOT NULL,
                               reason     VARCHAR(500) NOT NULL,
                               changed_by UUID NOT NULL REFERENCES users(id),
                               changed_at TIMESTAMP NOT NULL DEFAULT now(),
                               CONSTRAINT grade_history_old_value_check CHECK (old_value IS NULL OR (old_value >= 0 AND old_value <= 20)),
                               CONSTRAINT grade_history_new_value_check CHECK (new_value >= 0 AND new_value <= 20)
);

CREATE INDEX idx_grade_history_note ON grade_history(id_note);
CREATE INDEX idx_grade_history_changed_by ON grade_history(changed_by);

INSERT INTO specialty (code, label) VALUES
                                        ('NONE', 'Non défini'),
                                        ('EL', 'Écosysteme Logiciel'),
                                        ('TN', 'Transformation Numérique');