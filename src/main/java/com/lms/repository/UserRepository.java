package com.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lms.entity.User;
import com.lms.enums.Status;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByRoleId(int roleId);

    List<User> findByRoleId(Integer roleId);

    List<User> findByRole_RoleNameIn(List<String> roleNames);

    List<User> findByRoleRoleNameIn(List<String> roleNames);

    List<User> findByStatus(Status status);

    long countByStatus(Status status);

    long countByRole_RoleNameAndStatus(String roleName, Status status);

    List<User> findByStatusAndRole_RoleNameIn(Status status, List<String> roleNames);

    long countByStatusAndRole_RoleNameIn(Status status, List<String> roleNames);

    List<User> findByRoleIdAndStatus(int roleId, Status status);

    List<User> findByRole_RoleName(String roleName);

    List<User> findByRole_RoleNameAndStatus(String roleName, Status status);
}