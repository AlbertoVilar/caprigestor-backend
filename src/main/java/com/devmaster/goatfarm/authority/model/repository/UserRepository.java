package com.devmaster.goatfarm.authority.model.repository;

import com.devmaster.goatfarm.authority.api.projection.UserDetailsProjection;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
}
