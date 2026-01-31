package com.devmaster.goatfarm.authority.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.*;
import java.util.stream.Collectors;

import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome Ã© obrigatÃ³rio")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email Ã© obrigatÃ³rio")
    @Email(message = "Email deve ter formato vÃ¡lido")
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @NotBlank(message = "Senha Ã© obrigatÃ³ria")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Column(nullable = false, length = 60)
    private String password;

    @NotBlank(message = "CPF Ã© obrigatÃ³rio")
    @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 dÃ­gitos")
    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @NotNull(message = "UsuÃ¡rio deve ter pelo menos uma role")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tb_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<com.devmaster.goatfarm.farm.persistence.entity.GoatFarm> goatFarms = new ArrayList<>();



    public User() {}

    public User(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(Long id, String name, String email, String password, String cpf) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.cpf = cpf;
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public Boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getAuthority().equals(roleName));
    }

    public List<com.devmaster.goatfarm.farm.persistence.entity.GoatFarm> getGoatFarms() {
        return goatFarms;
    }

    public void setGoatFarms(List<com.devmaster.goatfarm.farm.persistence.entity.GoatFarm> goatFarms) {
        this.goatFarms = goatFarms;
    }

        public String getUsername() {
        return email;
    }

        @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

