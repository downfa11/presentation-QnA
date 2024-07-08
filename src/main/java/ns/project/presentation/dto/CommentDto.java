package ns.project.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private String commentId;
    private String userId;
    private String contents;
    private Long likesCount;
}