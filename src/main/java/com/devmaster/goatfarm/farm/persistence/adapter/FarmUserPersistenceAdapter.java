package com.devmaster.goatfarm.farm.persistence.adapter;

import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.application.ports.out.FarmUserPersistencePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Keeps the legacy farm-to-user JPA relation outside the Authority business core. */
@Component
public class FarmUserPersistenceAdapter implements FarmUserPersistencePort {
    private final UserRepository userRepository;
    private final UserBusiness userBusiness;

    public FarmUserPersistenceAdapter(UserRepository userRepository, UserBusiness userBusiness) {
        this.userRepository = userRepository;
        this.userBusiness = userBusiness;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findOrCreate(UserRequestVO request) {
        AuthorityAccount account = userBusiness.findOrCreateUser(request);
        return userRepository.findById(account.id())
                .orElseThrow(() -> new IllegalStateException("Usuário criado não encontrado"));
    }
}
