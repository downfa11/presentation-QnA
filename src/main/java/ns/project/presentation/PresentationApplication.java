package ns.project.presentation;

import lombok.RequiredArgsConstructor;
import ns.project.presentation.service.KakaoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
@SpringBootApplication
public class PresentationApplication {


	private final KakaoService kakaoService;

	public static void main(String[] args) {
		SpringApplication.run(PresentationApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@GetMapping("/rooms/{roomId}")
	public String index(@PathVariable String roomId){
		return "index.html";
	}

	@GetMapping("")
	public String main(Model model){

		model.addAttribute("kakaoUrl", kakaoService.getKakaoLogin());
		return "login.html";
	}

}
