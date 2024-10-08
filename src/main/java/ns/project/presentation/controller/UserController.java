package ns.project.presentation.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.service.UserService;
import ns.project.presentation.utils.jwtToken;
import ns.project.presentation.utils.jwtTokenProvider;
import org.springframework.http.*;
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
        response.put("userId",session.getAttribute("userId"));

         return ResponseEntity.ok(response);
    }

}
