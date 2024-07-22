package ns.project.presentation.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.service.UserService;
import ns.project.presentation.utils.jwtToken;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/user/login")
    public ResponseEntity<?> sendLoginRequest(@RequestParam String account, @RequestParam String password) {
        jwtToken token = userService.sendPostRequest(account, password);
        if (token!=null) {

            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
        }
    }

    @GetMapping("/api/user-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("nickname", session.getAttribute("nickname"));
        response.put("thumbnailImage", session.getAttribute("thumbnailImage"));
        response.put("jwtToken", session.getAttribute("jwtToken"));

         return ResponseEntity.ok(response);
    }

}
