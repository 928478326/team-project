package data_access.sqlite;

import entity.PasswordHasher;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Encodes persistent accounts' passwords with an independent random salt for each signup. */
public final class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final String FORMAT = "pbkdf2-sha256-v1";
    private static final int ITERATIONS = 600_000;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        Base64.Encoder encoder = Base64.getEncoder();
        return FORMAT + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(derive(password, salt));
    }

    @Override
    public boolean matches(String password, String encoded) {
        String[] parts = encoded.split(":", -1);
        if (parts.length != 3 || !FORMAT.equals(parts[0])) {
            return false;
        }
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt = decoder.decode(parts[1]);
            byte[] expected = decoder.decode(parts[2]);
            return salt.length == 16 && expected.length == 32
                    && MessageDigest.isEqual(expected, derive(password, salt));
        } catch (IllegalArgumentException invalidEncoding) {
            return false;
        }
    }

    private byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not process password.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
