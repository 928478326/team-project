package data_access.sqlite;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import entity.PasswordHasher;

/** Encodes persistent accounts' passwords with an independent random salt for each signup. */
public final class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final String FORMAT = "pbkdf2-sha256-v1";
    private static final String SEPARATOR = ":";
    private static final int ENCODING_PARTS = 3;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 600_000;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String password) {
        final byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        final Base64.Encoder encoder = Base64.getEncoder();
        return FORMAT + SEPARATOR + encoder.encodeToString(salt) + SEPARATOR
                + encoder.encodeToString(derive(password, salt));
    }

    @Override
    public boolean matches(String password, String encoded) {
        final String[] parts = encoded.split(SEPARATOR, -1);
        if (parts.length != ENCODING_PARTS || !FORMAT.equals(parts[0])) {
            return false;
        }
        try {
            final Base64.Decoder decoder = Base64.getDecoder();
            final byte[] salt = decoder.decode(parts[1]);
            final byte[] expected = decoder.decode(parts[2]);
            return salt.length == SALT_BYTES && expected.length == KEY_BITS / Byte.SIZE
                    && MessageDigest.isEqual(expected, derive(password, salt));
        }
        catch (IllegalArgumentException invalidEncoding) {
            return false;
        }
    }

    private byte[] derive(String password, byte[] salt) {
        final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not process password.", ex);
        }
        finally {
            spec.clearPassword();
        }
    }
}
