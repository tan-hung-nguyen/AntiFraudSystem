package com.tanhung.antifraudsystem.repo;

import com.tanhung.antifraudsystem.model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StolenCardRepo extends JpaRepository<StolenCard, Long> {
    boolean existsStolenCardByCardNumber(String number);
    void deleteStolenCardByCardNumber(String number);
}
