package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.ExamGroup;

@Component
public class ExamGroupValidator {

    public void validate(ExamGroup examGroup) {
        if (examGroup.getExamId() == null) {
            throw new IllegalArgumentException("examId is required");
        }
        if (examGroup.getGroupId() == null) {
            throw new IllegalArgumentException("groupId is required");
        }
    }
}
