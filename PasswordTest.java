import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "132747";
        String hash = encoder.encode(password);
        System.out.println("Hash gerado para '132747': " + hash);
        System.out.println("Verificação: " + encoder.matches(password, hash));
    }
}