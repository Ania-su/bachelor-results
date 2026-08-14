package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Group;

@Component
public class GroupValidator {

    public void validate(Group group) {
        if (group.getReference() == null || group.getReference().isBlank()) {
            throw new IllegalArgumentException("reference is required");
        }
        if (group.getReference().length() > 2) {
            throw new IllegalArgumentException("reference must be at most 2 characters");
        }
        if (group.getAcademicYear() == null) {
            throw new IllegalArgumentException("academicYear is required");
        }
    }
}