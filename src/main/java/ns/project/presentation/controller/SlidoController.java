package ns.project.presentation.controller;

import lombok.RequiredArgsConstructor;
import ns.project.presentation.service.SlidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SlidoController {

    private final SlidoService slidoService;

    @PostMapping("/rooms")
    public ResponseEntity<Void> createRoom(@RequestParam String userId) {
        slidoService.createRoom(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<String> findCurrentRoom(@PathVariable String roomId) {
        String currentRoom = slidoService.findCurrentRoom(roomId);
        if (currentRoom == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(currentRoom);
    }

    @PostMapping("/rooms/{roomId}/comments")
    public ResponseEntity<Void> registerComment(@PathVariable String roomId,
                                                @RequestParam String contents,
                                                @RequestParam String userId) {
        slidoService.registerComment(roomId, contents,userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rooms/{roomId}/comments/{commentId}")
    public ResponseEntity<Void> modifyComment(@PathVariable String roomId,
                                              @PathVariable String commentId,
                                              @RequestParam String contents) {
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

}
