package ns.project.presentation.controller;

import com.google.zxing.WriterException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.dto.CommentDto;
import ns.project.presentation.service.SlidoService;
import ns.project.presentation.utils.QRCodeGenerator;
import ns.project.presentation.utils.jwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SlidoController {

    private final SlidoService slidoService;
    private final jwtTokenProvider jwtProvider;

    @PostMapping("/rooms")
    public ResponseEntity<Void> createRoom(@RequestParam String userId) {
        slidoService.createRoom(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find/rooms/{roomId}")
    public ResponseEntity<String> findCurrentRoom(@PathVariable String roomId) {
        // token 검사 필요

        String currentRoom = slidoService.findCurrentRoom(roomId);
        if (currentRoom == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(currentRoom);
    }

    @PostMapping("/rooms/{roomId}/comments")
    public ResponseEntity<Void> registerComment(@PathVariable String roomId,
                                                @RequestBody CommentDto comment) {
        String contents = comment.getContents();
        String userId = comment.getUserId();
        System.out.println(comment);

        slidoService.registerComment(roomId, contents,userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rooms/{roomId}/comments/{commentId}")
    public ResponseEntity<Void> modifyComment(@PathVariable String roomId,
                                              @RequestBody CommentDto comment) {
        String contents = comment.getContents();
        String userId = comment.getUserId();
        String commentId = comment.getCommentId();

        slidoService.modifyComment(roomId, commentId, contents);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String roomId,
                                              @PathVariable String commentId) {
        slidoService.deleteComment(roomId, commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}/comments")
    public ResponseEntity<String> getCommentsInOrder(@PathVariable String roomId) {
        return ResponseEntity.ok(slidoService.getRoomAndComments(roomId));
    }

    @PostMapping("/rooms/{roomId}/comments/{commentId}/likes")
    public ResponseEntity<Long> updateLikes(@PathVariable String roomId,
                                            @PathVariable String commentId,
                                            @RequestParam String userId) {
        Long likesCount = slidoService.updateLikes(roomId, commentId, userId);
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/rooms/{roomId}/comments/{commentId}/likes")
    public ResponseEntity<Long> getLikesCount(@PathVariable String roomId,
                                              @PathVariable String commentId) {
        Long likesCount = slidoService.getLikesCount(roomId, commentId);
        return ResponseEntity.ok(likesCount);
    }


    @GetMapping("/qrcode/{roomId}")
    public ResponseEntity<byte[]> getQRCode(@PathVariable String roomId, HttpServletRequest request) {

        String baseURL = getBaseURL(request);  // 현재 요청의 기본 URL을 가져옵니다.
        String roomURL = baseURL + "/rooms/" + roomId;

        try {
            BufferedImage qrCodeImage = QRCodeGenerator.generateQRCodeImage(roomURL);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrCodeImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);

            return ResponseEntity.ok().headers(headers).body(imageBytes);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getBaseURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

    @GetMapping("/rooms/{roomId}/redirect")
    public ResponseEntity<String> redirectToRoom(@PathVariable String roomId, HttpServletRequest request) {
        String userId = jwtProvider.getMembershipIdbyToken();

        if (!jwtProvider.validateJwtToken(userId) || userId.equals("")) {
            String loginURL = getBaseURL(request) + "/index.html";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", loginURL);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            String roomURL = "/rooms/" + roomId;
            return ResponseEntity.ok(roomURL);
        }
    }
}

