package com.springbank.user.query.api.repositories;

import com.springbank.user.core.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    @Query("{'$or' : [{'firstname': {$regex : ?0, $options: 'i'}}, {'lastname': {$regex : ?0, $options: 'i'}}, {'emailAddress': {$regex : ?0, $options: 'i'}}, {'account.username': {$regex : ?0, $options: 'i'}}]}")
    List<User> findByFilterRegex(String filter);
}
