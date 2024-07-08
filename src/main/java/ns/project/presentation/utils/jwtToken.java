package ns.project.presentation.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class jwtToken {

    @Getter
    private final String membershipId;
    @Getter
    private final String jwtToken;


    public static jwtToken generateJwtToken(
            MembershipId membershipId,
            MembershipJwtToken membershipJwtToken) {

        return new jwtToken(
                membershipId.membershipId,
                membershipJwtToken.jwtToken
        );
    }

    @Value
    public static class MembershipId {
        public MembershipId(String value) {
            this.membershipId = value;
        }

        String membershipId;
    }

    @Value
    public static class MembershipJwtToken {
        public MembershipJwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
        }

        static String jwtToken;
    }

}