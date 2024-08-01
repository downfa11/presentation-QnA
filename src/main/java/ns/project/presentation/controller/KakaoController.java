package ns.project.presentation.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.dto.KakaoDTO;
import ns.project.presentation.service.KakaoService;
import ns.project.presentation.utils.jwtToken;
import ns.project.presentation.utils.jwtTokenProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("kakao")
public class KakaoController {

    private final KakaoService kakaoService;
    private final jwtTokenProvider jwtTokenProvider;

    @GetMapping("/callback")
    public String callback(HttpServletRequest request,
                           @RequestParam(value = "redirect", required = false) String redirect,
                           HttpServletResponse response, HttpSession session) throws Exception {
        KakaoDTO kakaoInfo = kakaoService.getKakaoInfo(request.getParameter("code"));

        Long id = kakaoInfo.getId();
        String jwt = jwtTokenProvider.generateJwtToken(String.valueOf(id));

        session.setAttribute("userId",id);
        session.setAttribute("nickname", kakaoInfo.getNickname());
        session.setAttribute("thumbnailImage", kakaoInfo.getThumbnailImageUrl());

        jwtTokenProvider.setJwtCookie(response, jwt);

        if (redirect != null) {
            return "redirect:" + redirect;
        }
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session,HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.getJwtFromCookie(request);
            if (token != null) {
                kakaoService.logout(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.invalidate();
        }

        return "redirect:/";
    }
}
