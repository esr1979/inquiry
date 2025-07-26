package com.kike.training.inquiry.domain.port.out;

import com.kike.training.inquiry.domain.model.User;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ListCrudRepository<User, Long>, UserRepositoryCustom {
    // Hereda métodos como save(), findAll(), findById(), etc.
    // `ListCrudRepository` devuelve List<T> en lugar de Iterable<T>, lo cual es más conveniente.
}