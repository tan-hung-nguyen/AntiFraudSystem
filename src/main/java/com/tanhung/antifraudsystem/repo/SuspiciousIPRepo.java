package com.tanhung.antifraudsystem.repo;

import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspiciousIPRepo extends JpaRepository<SuspiciousIPAddress, Long> {

    boolean existsByIpAddress(String ipAddress);

    void deleteByIpAddress(String ipAddress);
}
