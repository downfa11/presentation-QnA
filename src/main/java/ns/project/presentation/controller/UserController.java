package ns.project.presentation.controller;

import lombok.RequiredArgsConstructor;
import ns.project.presentation.service.UserService;
import ns.project.presentation.utils.jwtToken;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> sendLoginRequest(@RequestParam String account, @RequestParam String password) {
        jwtToken token = userService.sendPostRequest(account, password);
        if (token!=null) {

            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
        }
    }
}
