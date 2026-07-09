package com.tanhung.antifraudsystem.repo;

import com.tanhung.antifraudsystem.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepo extends JpaRepository<Region, Integer> {
    boolean existsByCode(String code);
    Region findByCode(String code);
}
