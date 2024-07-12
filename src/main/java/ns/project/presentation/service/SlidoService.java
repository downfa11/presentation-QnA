package ns.project.presentation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.dto.CommentDto;
import ns.project.presentation.dto.RoomDto;
import ns.project.presentation.utils.jwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SlidoService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final jwtTokenProvider jwtProvider;
    static final String ROOMS_KEY_PATTERN = "room:%s:timestamp:%s";
    static final String COMMENTS_KEY_PATTERN = "%s:comment:%s:%s";

    static final String LIKES_KEY_PATTERN = "%s:comment:%s:likes";
    static final String COMMENT_ID_KEY_PATTERN = "room:%s:commentId";


    @Value("${redis.expiredtime.room:60}")
    Long defaultTime;



    public void createRoom(String userId) {
        //String userId = jwtProvider.getMembershipIdbyToken();

        LocalDateTime current = LocalDateTime.now();
        String timestamp = current.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String key = String.format(ROOMS_KEY_PATTERN, userId, timestamp);
        redisTemplate.opsForValue().set(key, timestamp, defaultTime, TimeUnit.MINUTES);
        System.out.println("Room created with key: " + key);
    }

    public String findCurrentRoom(String roomId) {
        Set<String> keys = redisTemplate.keys(String.format(ROOMS_KEY_PATTERN, roomId, "*"));
        if (keys == null || keys.isEmpty()) {
            System.out.println("Room not found for roomId: " + roomId);
            return null;
        }

        String latestKey = null;
        long latestTimestamp = 0;

        for (String key : keys) {
            String[] parts = key.split(":");
            if (parts.length !=4)
                continue;

            String userId = parts[1];
            String timestampStr = parts[3];
            long timestamp = Long.parseLong(timestampStr);

            if (timestamp > latestTimestamp) {
                latestTimestamp = timestamp;
                latestKey = key;
            }
        }

        return latestKey;
    }



    @Transactional
    public void registerComment(String roomId, String contents, String userId) {
        //String userId = jwtProvider.getMembershipIdbyToken();
        String roomKey = findCurrentRoom(roomId);

        if (roomKey == null)
            return;

        String commentIdKey = String.format(COMMENT_ID_KEY_PATTERN, roomId);
        Long commentId = redisTemplate.opsForValue().increment(commentIdKey);

        String key = String.format(COMMENTS_KEY_PATTERN, roomKey, commentId, userId);

        LocalDateTime current = LocalDateTime.now();
        String timestamp = current.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        redisTemplate.opsForValue().set(key, contents+":timestamp:"+timestamp,defaultTime, TimeUnit.MINUTES);
       System.out.println("Comment registered with key: " + key);
    }


    @Transactional
    public void modifyComment(String roomId, String commentId, String contents) {
        String userId = jwtProvider.getMembershipIdbyToken();
        String roomKey = findCurrentRoom(roomId);

        if (roomKey == null)
            return;

        String commentKeyPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, commentId, "*");
        Set<String> keys = redisTemplate.keys(commentKeyPattern);

        if (keys == null || keys.isEmpty()) {
            System.out.println("Comment not found for commentId: " + roomId);
            return;
        }

        for (String key : keys) {
            String storedUserId = getUserIdFromCommentKey(key);
            if (userId.equals(storedUserId)) {
                LocalDateTime current = LocalDateTime.now();
                String timestamp = current.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                redisTemplate.opsForValue().set(key, contents+":timestamp:"+timestamp,defaultTime, TimeUnit.MINUTES);
                System.out.println("Comment modified with key: " + key);
                return;
            }
        }

        System.out.println("User is not authorized to modify this comment id:"+userId+", roomId:"+roomId);
    }

    @Transactional
    public void deleteComment(String roomId, String commentId) {
        String userId = jwtProvider.getMembershipIdbyToken();
        String roomKey = findCurrentRoom(roomId);

        if (roomKey == null)
            return;

        String commentKeyPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, commentId, "*");
        Set<String> keys = redisTemplate.keys(commentKeyPattern);

        if (keys == null || keys.isEmpty()) {
            System.out.println("Comment not found for commentId: " + roomId);
        }

        for (String key : keys) {
            String storedUserId = getUserIdFromCommentKey(key);
            if (userId.equals(storedUserId)) {
                String likesKey = String.format(LIKES_KEY_PATTERN, roomKey, commentId);
                if (redisTemplate.hasKey(likesKey)) {
                    redisTemplate.delete(likesKey);
                    System.out.println(likesKey + " has been deleted.");
                }

                redisTemplate.delete(key);
                System.out.println("Comment deleted with key: " + key);
                return;
            }
        }

    }

    private String getUserIdFromCommentKey(String commentKey) {
        String[] parts = commentKey.split(":");
        return parts[parts.length - 1];
    }


    @Transactional
    public Long updateLikes(String roomId, String commentId,String userId) {
        //String userId = jwtProvider.getMembershipIdbyToken().toString();

        if (isUserLiked(roomId, commentId, userId))
            removeLike(roomId, commentId, userId);
        else
            addLike(roomId, commentId, userId);


        Long likesCount = getLikesCount(roomId, commentId);
        System.out.println("Updated likes count: " + likesCount);
        return likesCount;
    }

    public Boolean isUserLiked(String roomId, String commentId, String userId) {
        String key = String.format(LIKES_KEY_PATTERN, roomId, commentId);
        Boolean result = redisTemplate.opsForSet().isMember(key, userId);
        return result;
    }

    public Long addLike(String roomId, String commentId, String userId) {
        String key = String.format(LIKES_KEY_PATTERN, roomId, commentId);
        Long result = redisTemplate.opsForSet().add(key, userId);
        return result;
    }

    public Long removeLike(String roomId, String commentId, String userId) {
        String key = String.format(LIKES_KEY_PATTERN, roomId, commentId);
        Long result = redisTemplate.opsForSet().remove(key, userId);
        return result;
    }

    public Long getLikesCount(String roomId, String commentId) {
        String key = String.format(LIKES_KEY_PATTERN, roomId, commentId);
        Long result = redisTemplate.opsForSet().size(key);
        return result;
    }

    @Transactional
    public String getRoomAndComments(String roomId) {
        String roomKey = findCurrentRoom(roomId);

        if (roomKey == null)
            return "Not found room :"+roomId;

        String[] roomKeyParts = roomKey.split(":");
        String userId = roomKeyParts[1];
        String roomTimestamp = roomKeyParts[3];

        String commentsPattern = String.format(COMMENTS_KEY_PATTERN, roomKey, "*", "*");
        Set<String> commentKeys = redisTemplate.keys(commentsPattern);

        List<CommentDto> commentDtos = new ArrayList<>();
        for (String commentKey : commentKeys) {
            String[] commentKeyParts = commentKey.split(":");

            String commentId = commentKeyParts[commentKeyParts.length - 2];
            String commentUserId = commentKeyParts[commentKeyParts.length - 1];

            String[] contentsInfo = redisTemplate.opsForValue().get(commentKey).split(":");
            String contents = contentsInfo[0];
            String contentsCreatedAt = contentsInfo[2];

            Long likesCount = getLikesCount(roomKey, commentId);

            CommentDto commentDto = new CommentDto(commentId, commentUserId, contents,contentsCreatedAt, likesCount);
            commentDtos.add(commentDto);
        }

        RoomDto roomDto = new RoomDto(roomId, roomTimestamp, commentDtos);
        String result;
        try {
            result = objectMapper.writeValueAsString(roomDto);
        }catch (JsonProcessingException e) {
            System.out.println("JSON parsing error: " + e);
            return "Json parsing error: "+e;
        }
        System.out.println("list : "+result);
        return result;
    }

}
