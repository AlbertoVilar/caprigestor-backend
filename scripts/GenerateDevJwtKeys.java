import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * Generates local-only RSA keys for the development profile.
 *
 * <p>Run with {@code java scripts/GenerateDevJwtKeys.java}. The files are
 * created outside the repository at {@code ~/.caprigestor/keys} and are never
 * overwritten.</p>
 */
public final class GenerateDevJwtKeys {

    private GenerateDevJwtKeys() {
    }

    public static void main(String[] args) throws Exception {
        Path outputDirectory = args.length == 0
                ? Path.of(System.getProperty("user.home"), ".caprigestor", "keys")
                : Path.of(args[0]);
        Path privateKey = outputDirectory.resolve("app.key");
        Path publicKey = outputDirectory.resolve("app.pub");

        if (Files.exists(privateKey) || Files.exists(publicKey)) {
            throw new IllegalStateException("JWT key files already exist at " + outputDirectory
                    + ". Refusing to overwrite them.");
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        Files.createDirectories(outputDirectory);
        writePem(privateKey, "PRIVATE KEY", pair.getPrivate().getEncoded());
        writePem(publicKey, "PUBLIC KEY", pair.getPublic().getEncoded());

        System.out.println("Local JWT keys created at " + outputDirectory);
    }

    private static void writePem(Path path, String type, byte[] bytes) throws Exception {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(bytes);
        String pem = "-----BEGIN " + type + "-----\n" + encoded
                + "\n-----END " + type + "-----\n";
        Files.writeString(path, pem, StandardCharsets.US_ASCII);
    }
}
