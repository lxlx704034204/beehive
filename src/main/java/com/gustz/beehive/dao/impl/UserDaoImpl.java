package com.gustz.beehive.dao.impl;

import com.gustz.beehive.config.log.TestLogger;
import com.gustz.beehive.dao.UserDao;
import org.springframework.stereotype.Repository;

/**
 * XXX
 *
 * @author zhangzhenfeng
 * @since 2016-01-08
 */
@Repository
public class UserDaoImpl implements UserDao {

    @Override
    public void listUser() {
        //Integer.parseInt("fdsfdfd");
        System.out.println("listUser...");
    }
}
