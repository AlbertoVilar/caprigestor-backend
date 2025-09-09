// Vamos usar uma abordagem mais simples
public class GenerateHash {
    public static void main(String[] args) {
        // Vamos criar um SQL para atualizar a senha
        String sql = "UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye' WHERE email = 'albertovilar1@gmail.com';";
        System.out.println("Execute este SQL no banco:");
        System.out.println(sql);
        System.out.println();
        System.out.println("Ou use o Spring Boot para gerar o hash correto.");
    }
}