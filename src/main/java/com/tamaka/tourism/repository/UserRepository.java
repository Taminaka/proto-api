package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE :roles MEMBER OF u.roles")
    List<User> findByRolesIn(@Param("roles") Set<com.tamaka.tourism.entity.Role> roles);

    @Query("SELECT u FROM User u WHERE u.status != com.tamaka.tourism.entity.UserStatus.INACTIVE")
    List<User> findActiveUsers();
}