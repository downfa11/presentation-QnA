package ns.project.presentation.service;

import lombok.RequiredArgsConstructor;
import ns.project.presentation.utils.jwtToken;
import ns.project.presentation.utils.jwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {


    private final RestTemplate restTemplate;
    private final jwtTokenProvider jwtProvider;

    @Value("${login.url.endpoint:}")
    private String url;

    @Value("${user.account:id:}")
    private String account;

    @Value("${user.account:password:}")
    private String password;

    public jwtToken sendPostRequest(String id, String pw) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(account, id);
        requestBody.put(password, pw);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if(response.getStatusCode() == HttpStatus.OK){
                String jwt = jwtProvider.generateJwtToken(id);
                System.out.println("[success] External Auth token: "+jwt);
                return jwtToken.generateJwtToken(new jwtToken.MembershipId(id.toString()),
                        new jwtToken.MembershipJwtToken(jwt));
            }
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return null;
    }
}
