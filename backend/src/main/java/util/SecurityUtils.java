package util;

import io.quarkus.elytron.security.common.BcryptUtil;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BcryptUtil.bcryptHash(plainPassword);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
