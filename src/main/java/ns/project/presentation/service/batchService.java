package ns.project.presentation.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.dto.CommentDto;
import ns.project.presentation.dto.RoomDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ns.project.presentation.service.SlidoService.*;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class batchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SlidoService slidoService;

    @Value("${redis.expiredtime.comment:20}")
    Long defaultTime;

    @Scheduled(fixedRateString = "${batch.fixedRate:3000}") // 3sec. 60000=1min
    public void processAllRoomComments() {
        Set<String> roomKeys = redisTemplate.keys("room:*:timestamp:*");
        if (roomKeys == null || roomKeys.isEmpty())
            return;


        Set<String> processedRooms = new HashSet<>();

        for (String roomKey : roomKeys) {
            String[] parts = roomKey.split(":");
            if (parts.length != 4)
                continue;

            String roomId = parts[1];

            if (!processedRooms.contains(roomId)) {
                processCommentsForRoom(roomId);
                processedRooms.add(roomId);
            }
        }
    }

    private void processCommentsForRoom(String roomId) {
        String roomKey = slidoService.findCurrentRoom(roomId);

        String commentsPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, "*", "*");
        Set<String> commentKeys = redisTemplate.keys(commentsPattern);

        if (commentKeys == null || commentKeys.isEmpty()) {
            // System.out.println("No comments found for roomId: " + roomId);
            return;
        }
        LocalDateTime mostRecentTimestamp = null;

        for (String commentKey : commentKeys) {
            String contents = redisTemplate.opsForValue().get(commentKey);
            String timestampStr = contents.split("timestamp:")[1];
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            if (mostRecentTimestamp == null || timestamp.isAfter(mostRecentTimestamp)) {
                mostRecentTimestamp = timestamp;
            }
        }

        if (mostRecentTimestamp != null && mostRecentTimestamp.plusMinutes(defaultTime).isBefore(LocalDateTime.now())) {

            List<CommentDto> commentDtos = new ArrayList<>();
            for (String commentKey : commentKeys) {
                String[] commentKeyParts = commentKey.split(":");
                String commentId = commentKeyParts[2];
                String commentUserId = commentKeyParts[3];
                String contents = redisTemplate.opsForValue().get(commentKey);

                Long likesCount = slidoService.getLikesCount(roomKey, commentId);

                CommentDto commentDto = new CommentDto(commentId, commentUserId, contents, likesCount);
                commentDtos.add(commentDto);
            }

            RoomDto roomDto = new RoomDto(roomId, roomKey.split(":")[3], commentDtos);
            try {
                String result = objectMapper.writeValueAsString(roomDto);
                System.out.println("room deleted: " + result);
                deleteRoom(roomId);
            } catch (JsonProcessingException e) {
                System.out.println("JSON parsing error: " + e);
            }
        }
    }

    private void deleteRoom(String roomId) {
        Set<String> roomKeys = redisTemplate.keys(String.format("room:%s:timestamp:*", roomId));
        if (roomKeys != null) {
            for (String roomKey : roomKeys) {
                String commentsPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, "*", "*");
                Set<String> commentKeys = redisTemplate.keys(commentsPattern);
                if (commentKeys != null) {
                    for (String commentKey : commentKeys) {
                        redisTemplate.delete(commentKey);
                    }
                }

                String likesPattern = String.format(LIKES_KEY_PATTERN, roomKey, "*");
                Set<String> likeKeys = redisTemplate.keys(likesPattern);
                if (likeKeys != null) {
                    for (String likeKey : likeKeys) {
                        redisTemplate.delete(likeKey);
                    }
                }

                redisTemplate.delete(roomKey);
                String commentIdKey = String.format(COMMENT_ID_KEY_PATTERN, roomId);
                redisTemplate.delete(commentIdKey);
            }
        }
    }
}
