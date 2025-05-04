package com.devmaster.goatfarm.authority.facade;

import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import org.springframework.stereotype.Service;

@Service
public class UserFacade {

    private final UserBusiness business;

    public UserFacade(UserBusiness business) {
        this.business = business;

    }

    public UserResponseVO getMe() {
        return business.getMe();
    }
}
