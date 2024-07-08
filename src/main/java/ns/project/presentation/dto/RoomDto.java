package ns.project.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class RoomDto {

    private String roomId;
    private String roomTimestamp;
    private List<CommentDto> comments;
}
