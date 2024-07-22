package ns.project.presentation.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import ns.project.presentation.dto.KakaoDTO;
import ns.project.presentation.service.KakaoService;
import ns.project.presentation.utils.jwtTokenProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("kakao")
public class KakaoController {

    private final KakaoService kakaoService;
    private final jwtTokenProvider jwtTokenProvider;

    @GetMapping("/callback")
    public String callback(HttpServletRequest request, HttpSession session) throws Exception {
        KakaoDTO kakaoInfo = kakaoService.getKakaoInfo(request.getParameter("code"));

        Long id = kakaoInfo.getId();
        String jwt = jwtTokenProvider.generateJwtToken(String.valueOf(id));

        session.setAttribute("nickname", kakaoInfo.getNickname());
        session.setAttribute("thumbnailImage", kakaoInfo.getThumbnailImageUrl());
        session.setAttribute("jwtToken", jwt);

        return "redirect:/";
    }



}
