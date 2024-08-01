package ns.project.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private String commentId;
    @NotNull
    private String userId;
    @NotNull
    private String username;
    @NotNull
    private String contents;
    @NotNull
    private String thumbnailImage;

    private String date;
    private Long likesCount;
}