package com.tanhung.antifraudsystem.repo;

import com.tanhung.antifraudsystem.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
    Role findRoleByRoleValue(String roleValue);

}
