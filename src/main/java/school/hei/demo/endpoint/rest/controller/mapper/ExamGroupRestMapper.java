package school.hei.demo.endpoint.rest.controller.mapper;
import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.ExamGroupRequest;
import school.hei.demo.domain.dto.response.ExamGroupResponse;
import school.hei.demo.entity.ExamGroup;

@Component
public class ExamGroupRestMapper {

    public ExamGroupResponse toResponse(ExamGroup eg) {
        if (eg == null) return null;
        return ExamGroupResponse.builder()
                .id(eg.getId())
                .examId(eg.getExamId())
                .groupId(eg.getGroupId())
                .build();
    }

    public ExamGroup toDomain(ExamGroupRequest request) {
        return ExamGroup.builder()
                .groupId(request.getGroupId())
                .build();
    }
}
