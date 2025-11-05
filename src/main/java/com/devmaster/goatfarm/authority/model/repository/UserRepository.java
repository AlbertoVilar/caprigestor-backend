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

    @Query("DELETE FROM User u WHERE u.email != :email")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByEmailNot(String email);

    @Query("DELETE FROM GoatFarm gf WHERE gf.user.email != :email")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteGoatFarmsByUserEmailNot(String email);

    @Query(nativeQuery = true, value = "DELETE FROM eventos WHERE goat_registration_number IN (SELECT g.num_registro FROM cabras g JOIN capril c ON g.capril_id = c.id WHERE c.user_id != :adminId)")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteEventsFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM cabras WHERE capril_id IN (SELECT c.id FROM capril c WHERE c.user_id != :adminId)")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteGoatsFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM telefone WHERE goat_farm_id IN (SELECT c.id FROM capril c WHERE c.user_id != :adminId)")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deletePhonesFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM endereco WHERE id IN (SELECT c.address_id FROM capril c WHERE c.user_id != :adminId AND c.address_id IS NOT NULL)")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteAddressesFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM capril WHERE user_id != :adminId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteGoatFarmsFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM tb_user_role WHERE user_id != :adminId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteRolesFromOtherUsers(Long adminId);

    @Query(nativeQuery = true, value = "DELETE FROM users WHERE id != :adminId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteOtherUsers(Long adminId);

    default void cleanDatabaseStepByStep(Long adminId) {
        deleteEventsFromOtherUsers(adminId);
        deleteGoatsFromOtherUsers(adminId);
        deletePhonesFromOtherUsers(adminId);
        deleteGoatFarmsFromOtherUsers(adminId);
        deleteAddressesFromOtherUsers(adminId);
        deleteRolesFromOtherUsers(adminId);
        deleteOtherUsers(adminId);
    }
}
