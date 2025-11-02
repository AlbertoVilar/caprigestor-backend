package com.devmaster.goatfarm.authority.model.repository;

import com.devmaster.goatfarm.authority.api.projection.UserDetailsProjection;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query(nativeQuery = true, value = """
            SELECT users.email AS username, 
                   users.password, 
                   role.id AS roleId, 
                   role.authority
            FROM users
            INNER JOIN tb_user_role ON users.id = tb_user_role.user_id
            INNER JOIN role ON role.id = tb_user_role.role_id
            WHERE users.email = :email
            """)
	List<UserDetailsProjection> searchUserAndRolesByEmail(String email);

	@EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    Optional<User> findByCpf(String cpf);

    @Query(nativeQuery = true, value = "DELETE FROM tb_user_role WHERE user_id != :adminId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteRolesFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM users WHERE id != :adminId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteOtherUsers(Long adminId);
}
