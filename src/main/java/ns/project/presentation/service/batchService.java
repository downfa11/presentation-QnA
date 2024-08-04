package ns.project.presentation.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ns.project.presentation.service.SlidoService.*;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class batchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SlidoService slidoService;

    @Value("${redis.expiredtime.comment:60}")
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
        if (roomKey == null) {
            return;
        }

        String commentsPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, "*", "*");
        Set<String> commentKeys = redisTemplate.keys(commentsPattern);

        if (commentKeys == null || commentKeys.isEmpty()) {
            return;
        }

        LocalDateTime mostRecentTimestamp = null;

        for (String commentKey : commentKeys) {
            String contents = redisTemplate.opsForValue().get(commentKey);
            try {
                String timestampStr = contents.split("timestamp:")[1];
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                if (mostRecentTimestamp == null || timestamp.isAfter(mostRecentTimestamp)) {
                    mostRecentTimestamp = timestamp;
                }
            } catch (Exception e) {
                System.err.println("Error parsing timestamp from comment: " + commentKey);
            }
        }

        if (mostRecentTimestamp != null && mostRecentTimestamp.plusMinutes(defaultTime).isBefore(LocalDateTime.now())) {
            System.out.println("room deleted: " + roomId);
            try {
                backupRoomData(roomId);
            } catch (IOException e) {
                System.err.println("Failed to backup room data: " + e.getMessage());
            }
            deleteRoom(roomId);
        }
    }

    private void backupRoomData(String roomId) throws IOException {
        Map<String, Object> backupData = new HashMap<>();

        Set<String> roomKeys = redisTemplate.keys(String.format("room:%s:timestamp:*", roomId));
        if (roomKeys != null) {
            for (String roomKey : roomKeys) {
                String roomData = redisTemplate.opsForValue().get(roomKey);
                backupData.put(roomKey, roomData);

                String commentsPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, "*", "*");
                Set<String> commentKeys = redisTemplate.keys(commentsPattern);
                if (commentKeys != null) {
                    for (String commentKey : commentKeys) {
                        String commentData = redisTemplate.opsForValue().get(commentKey);
                        backupData.put(commentKey, commentData);
                    }
                }

                String likesPattern = String.format(LIKES_KEY_PATTERN, roomKey, "*");
                Set<String> likeKeys = redisTemplate.keys(likesPattern);
                if (likeKeys != null) {
                    for (String likeKey : likeKeys) {
                        String likeData = redisTemplate.opsForValue().get(likeKey);
                        backupData.put(likeKey, likeData);
                    }
                }
            }
        }

        String backupJson = objectMapper.writeValueAsString(backupData);
        System.out.println("delete room : "+backupJson);
        saveBackupToFile(roomId, backupJson);
    }

    private void saveBackupToFile(String roomId, String backupJson) {
        String backupKey = String.format("backup:room:%s:%s", roomId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        redisTemplate.opsForValue().set(backupKey, backupJson);
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
