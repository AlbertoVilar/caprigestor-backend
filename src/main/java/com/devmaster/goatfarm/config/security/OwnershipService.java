package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import org.springframework.stereotype.Service;

/**
 * Service responsável por centralizar e aplicar regras de autorização baseadas
 * em ownership (propriedade de recursos) dentro do domínio da aplicação.
 *
 * <p>
 * Este serviço garante que o usuário autenticado tenha permissão para acessar
 * ou manipular recursos sensíveis, como fazendas e cabras, antes que qualquer
 * regra de negócio seja executada.
 * </p>
 *
 * <p>
 * Regras importantes:
 * <ul>
 *   <li>Usuários com role <b>ROLE_ADMIN</b> possuem bypass total de ownership.</li>
 *   <li>Usuários comuns só podem acessar recursos dos quais são proprietários.</li>
 *   <li>Falhas de autorização resultam em {@link UnauthorizedException}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Este serviço é utilizado pela camada de Business e não deve conter lógica
 * relacionada a HTTP ou controllers.
 * </p>
 */
@Service
public class OwnershipService {

    private final GoatFarmDAO goatFarmDAO;
    private final UserDAO userDAO;
    private final GoatDAO goatDAO;

    public OwnershipService(GoatFarmDAO goatFarmDAO, UserDAO userDAO, GoatDAO goatDAO) {
        this.goatFarmDAO = goatFarmDAO;
        this.userDAO = userDAO;
        this.goatDAO = goatDAO;
    }

    /**
     * Verifica se o usuário autenticado possui permissão para acessar
     * a fazenda informada.
     *
     * <p>
     * A verificação segue as seguintes regras:
     * <ul>
     *   <li>Usuários com role <b>ROLE_ADMIN</b> têm acesso irrestrito.</li>
     *   <li>Usuários comuns devem ser proprietários da fazenda.</li>
     * </ul>
     * </p>
     *
     * @param farmId identificador da fazenda a ser validada
     *
     * @throws UnauthorizedException caso o usuário não seja proprietário da fazenda
     *                               ou a fazenda não possua proprietário válido
     */
    public void verifyFarmOwnership(Long farmId) {
        User current = userDAO.getAuthenticatedEntity();

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));

        if (isAdmin) {
            return;
        }

        var farm = goatFarmDAO.findFarmEntityById(farmId);

        if (farm.getUser() == null || !farm.getUser().getId().equals(current.getId())) {
            throw new UnauthorizedException("Usuário não é proprietário desta fazenda.");
        }
    }

    /**
     * Verifica se o usuário autenticado possui permissão para acessar
     * uma cabra específica dentro de uma fazenda.
     *
     * <p>
     * A verificação ocorre em duas etapas:
     * <ol>
     *   <li>Confirma se o usuário é proprietário da fazenda (ou admin).</li>
     *   <li>Confirma se a cabra pertence à fazenda informada.</li>
     * </ol>
     * </p>
     *
     * @param farmId identificador da fazenda
     * @param goatId identificador da cabra
     *
     * @throws UnauthorizedException caso a cabra não pertença à fazenda
     *                               ou o usuário não possua permissão
     */
    public void verifyGoatOwnership(Long farmId, String goatId) {
        // Primeiro valida o ownership da fazenda (admin possui bypass)
        verifyFarmOwnership(farmId);

        // Em seguida garante que a cabra pertence à fazenda informada
        var goatOpt = goatDAO.findByIdAndFarmId(goatId, farmId);
        if (goatOpt.isEmpty()) {
            throw new UnauthorizedException("Cabra não pertence à fazenda informada.");
        }
    }

    /**
     * Retorna o usuário atualmente autenticado no sistema.
     *
     * <p>
     * Este método é um atalho para centralizar o acesso ao usuário
     * autenticado, evitando chamadas diretas ao {@link UserDAO}
     * em camadas superiores.
     * </p>
     *
     * @return entidade {@link User} do usuário autenticado
     */
    public User getCurrentUser() {
        return userDAO.getAuthenticatedEntity();
    }

    /**
     * Verifica se o usuário autenticado possui perfil de administrador.
     *
     * @return {@code true} se o usuário possuir role {@code ROLE_ADMIN},
     *         {@code false} caso contrário
     */
    public boolean isCurrentUserAdmin() {
        User current = userDAO.getAuthenticatedEntity();
        return current.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
    }
}
