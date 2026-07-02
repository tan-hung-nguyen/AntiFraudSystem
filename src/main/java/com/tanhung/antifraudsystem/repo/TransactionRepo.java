package com.tanhung.antifraudsystem.repo;

import com.tanhung.antifraudsystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    @Query(value = """
        SELECT DISTINCT t.region.code
        FROM Transaction t
        WHERE t.cardNumber = :number
            AND t.date BETWEEN :from AND :to
            AND t.region.code <> :region
    """)
    List<String> findDistinctRegionExcluding(
            @Param("number") String cardNumber,
            @Param("from")LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("region") String region
            );

    @Query("SELECT DISTINCT t.ip FROM Transaction t " +
            "WHERE t.cardNumber = :number AND t.date BETWEEN :from AND :to " +
            "AND t.ip <> :ip")
    List<String> findDistinctIpsExcluding(
            @Param("number") String number,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("ip") String ip);
}
