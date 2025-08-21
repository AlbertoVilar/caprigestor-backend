package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserBusiness {

    private final UserDAO userDAO;

    public UserBusiness(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User findByUsername(String username) {
        return userDAO.findUserByUsername(username);
    }

    public UserResponseVO getMe() {
        return userDAO.getMe();
    }

    public UserResponseVO saveUser(UserRequestVO vo) {
        return userDAO.saveUser(vo);
    }
}
