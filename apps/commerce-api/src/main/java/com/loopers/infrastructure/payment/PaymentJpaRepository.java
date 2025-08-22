package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    @Query("""
        select p from Payment p
        where p.txKey = :txKey
    """)
    Optional<Payment> findByTxKey(String txKey);

    @Query("""
        select p
          from Payment p
         where p.status = :status
           and p.createdAt >= :since
         order by p.createdAt asc
    """)
    List<Payment> findRecentWaiting(
            @Param("since") LocalDateTime since,
            @Param("status") Payment.Status status
    );
}
