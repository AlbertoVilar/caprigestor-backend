package com.devmaster.goatfarm.article;

import com.devmaster.goatfarm.article.enums.ArticleCategory;
import com.devmaster.goatfarm.article.model.entity.Article;
import com.devmaster.goatfarm.article.model.repository.ArticleRepository;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArticlePublicAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private User adminUser;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role roleAdmin = roleRepository.save(new Role("ROLE_ADMIN", "Admin"));

        adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setCpf("00000000000");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.addRole(roleAdmin);
        userRepository.save(adminUser);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginPayload = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void publicListShouldReturnOnlyPublished() throws Exception {
        Article published = Article.builder()
                .title("Artigo Publicado")
                .slug("artigo-publicado")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.MANEJO)
                .published(true)
                .publishedAt(LocalDateTime.now().minusDays(1))
                .highlighted(false)
                .build();
        Article draft = Article.builder()
                .title("Artigo Rascunho")
                .slug("artigo-rascunho")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.SAUDE)
                .published(false)
                .publishedAt(null)
                .highlighted(false)
                .build();
        articleRepository.saveAll(List.of(published, draft));

        mockMvc.perform(get("/public/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].slug").value("artigo-publicado"));
    }

    @Test
    void publicSlugShouldReturn404WhenNotPublished() throws Exception {
        Article draft = Article.builder()
                .title("Artigo Rascunho")
                .slug("artigo-rascunho")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.SAUDE)
                .published(false)
                .publishedAt(null)
                .highlighted(false)
                .build();
        articleRepository.save(draft);

        mockMvc.perform(get("/public/articles/artigo-rascunho"))
                .andExpect(status().isNotFound());
    }

    @Test
    void highlightsShouldReturnThreeArticlesFollowingRules() throws Exception {
        Article highlighted1 = Article.builder()
                .title("Destaque 1")
                .slug("destaque-1")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.REPRODUCAO)
                .published(true)
                .publishedAt(LocalDateTime.now().minusDays(1))
                .highlighted(true)
                .build();
        Article highlighted2 = Article.builder()
                .title("Destaque 2")
                .slug("destaque-2")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.REPRODUCAO)
                .published(true)
                .publishedAt(LocalDateTime.now().minusDays(2))
                .highlighted(true)
                .build();
        Article recentPublished = Article.builder()
                .title("Recente")
                .slug("recente")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.MANEJO)
                .published(true)
                .publishedAt(LocalDateTime.now())
                .highlighted(false)
                .build();
        Article olderPublished = Article.builder()
                .title("Antigo")
                .slug("antigo")
                .excerpt("Resumo")
                .contentMarkdown("Conteudo")
                .category(ArticleCategory.MANEJO)
                .published(true)
                .publishedAt(LocalDateTime.now().minusDays(10))
                .highlighted(false)
                .build();
        articleRepository.saveAll(List.of(highlighted1, highlighted2, recentPublished, olderPublished));

        mockMvc.perform(get("/public/articles/highlights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].slug", Matchers.containsInAnyOrder("destaque-1", "destaque-2", "recente")));
    }

    @Test
    void adminShouldCreateUpdatePublishAndHighlight() throws Exception {
        String token = loginAndGetToken("admin@example.com", "password");

        String createPayload = """
                {
                  "title": "Titulo Inicial",
                  "excerpt": "Resumo inicial",
                  "contentMarkdown": "Conteudo inicial",
                  "category": "MANEJO",
                  "coverImageUrl": "https://example.com/cover.jpg"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published").value(false))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long articleId = createdJson.get("id").asLong();

        String updatePayload = """
                {
                  "title": "Titulo Atualizado",
                  "excerpt": "Resumo atualizado",
                  "contentMarkdown": "Conteudo atualizado",
                  "category": "SAUDE",
                  "coverImageUrl": "https://example.com/cover2.jpg"
                }
                """;

        mockMvc.perform(put("/api/articles/" + articleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("titulo-atualizado"));

        String publishPayload = "{\"published\": true}";
        mockMvc.perform(patch("/api/articles/" + articleId + "/publish")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());

        String highlightPayload = "{\"highlighted\": true}";
        mockMvc.perform(patch("/api/articles/" + articleId + "/highlight")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(highlightPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlighted").value(true));
    }

    @Test
    void adminShouldRejectDuplicateSlug() throws Exception {
        String token = loginAndGetToken("admin@example.com", "password");

        String createPayload = """
                {
                  "title": "Titulo Unico",
                  "excerpt": "Resumo inicial",
                  "contentMarkdown": "Conteudo inicial",
                  "category": "MANEJO",
                  "coverImageUrl": null
                }
                """;

        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isConflict());
    }
}
