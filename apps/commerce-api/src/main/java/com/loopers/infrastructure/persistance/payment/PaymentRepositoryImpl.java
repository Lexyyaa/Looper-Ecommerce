package com.loopers.infrastructure.persistance.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByTxKey(String txKey) {
        return jpaRepository.findByTxKey(txKey);
    }

    @Override
    public List<Payment> findRecentWaiting(LocalDateTime since,Payment.Status status) {
        return jpaRepository.findRecentWaiting(since,status);
    }
}
