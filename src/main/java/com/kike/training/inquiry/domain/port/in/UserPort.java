package com.kike.training.inquiry.domain.port.in;

import java.util.List;
import java.util.Optional;
import com.kike.training.inquiry.domain.model.User;


public interface UserPort {

    User saveUser(User user, String dataSourceId);

    List<User> getAllUsers(String dataSourceId);

    Optional<User> getUserById(Long id, String dataSourceId);

}