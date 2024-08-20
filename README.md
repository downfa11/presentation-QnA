# Presentation QnA using Redis

Audience Interaction Made Easy - Slido-inspired toy project

Implemented simply using Redis.

This project is a **container-based application**, and user login is fetched from the web service API.

**Therefore, it requires a backend that can be executed together.**

</br></br>
Presentation QnA는 **세미나 발표에서 질의 응답시 원활한 소통을 위해 제공**되는 서비스입니다.

참가자는 발표자가 만든 방에 QR코드를 촬영해 모바일로 접속하고, 발표자는 스크린 화면을 통해 가장 공감을 많이 얻은 질문부터 답변할 수 있습니다.

- 백엔드 수준의 `XSS`, `SQL Injection` 취약점에 대한 시큐어 코딩

### **기능**

- **사용자 인증** : 카카오 로그인을 통해 인증된 사용자만 질의응답 방에 접근 가능
- **JWT 인가 처리** : 카카오 계정의 ID를 클라이언트 쿠키에 JWT로 삽입하여 관리
- **모바일 접근** : 참석자는 발표자의 화면에 표시된 QR코드를 통해 모바일에서 접속

모든 시스템은 RDBMS를 두지 않고 Redis만 이용했습니다.

Redis의 Key-Value 방식에선 데이터 저장시 ‘:’ 문자를 포함한 키를 문자열로 처리하기에, **Slicing 과정에서 발생할 수 있는 혼란을 피하기 위해 문자열로 인식시키는 방법**을 사용했습니다.

</br></br>

## Start to Do
In docker-compose.yaml, set the following:

- `LOGIN_URL_ENDPOINT` : URL of the web service for login
- `USER_ACCOUNT_ID` : Login account ID
- `USER_ACCOUNT_PASWORD` : Login password  
</br>  

- `REDIS_EXPIREDTIME_ROOM` : Room life cycle (minutes, default=60)

- `REDIS_EXPIREDTIME_COMMENT` : The room will be deleted if this time elapses since the last comment (minutes, default=20)

- `BATCH_FIXEDRATE` : Interval for scheduling batch (milliseconds, default=3000)

</br></br>



## Service image (kakao login)

![Untitled.png](Untitled.png)

## Service image (mobile)
<img src="naver.jpg" alt="Naver" width="250" height="500">
