package com.loopers.domain.payment;

import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.domain.pg.PgClientAdapter;
import com.loopers.infrastructure.payment.PaymentJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PgClientAdapter pgClientAdapter;

    @Transactional
    public Payment createPending(Long userId, Long orderId, Long amount, Payment.Method method) {
        Payment p = Payment.create(userId, orderId, amount, method);
        return paymentRepository.save(p);
    }

    public Payment save(Payment payment) {
        payment.pay(payment.getUserId());
        return paymentRepository.save(payment);
    }

    public Payment confirmPayment(Payment payment) {
        payment.toSuccess();
        return paymentRepository.save(payment);
    }

    public Payment cancelPayment(Payment payment,String msg) {
        payment.toFail(msg);
        return paymentRepository.save(payment);
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 결제입니다."));
    }

    @Transactional
    public Payment bindTxKeyOnPending(Payment payment, String txKey) {
            payment.toRequested(txKey);
            Payment saved = paymentRepository.save(payment);
            return saved;
    }

    @Transactional(readOnly = true)
    public Payment findByTxKey(String txKey) {
        return paymentRepository.findByTxKey(txKey).orElseThrow(
                () -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 결제입니다.")
        );
    }

    @Transactional(readOnly = true)
    public PaymentDetail getUniquePaymentDetail(Long orderId, String currentTxKey) {
        Optional<PaymentDetail> unique = pgClientAdapter.findSingleSuccessPayment(String.valueOf(orderId));
        if (unique.isPresent()) {
            return unique.get();
        }
        return pgClientAdapter.getByTxKey(currentTxKey);
    }

    @Transactional(readOnly = true)
    public List<Payment> getRecentWaiting(LocalDateTime since) {
        return paymentRepository.findRecentWaiting(since,Payment.Status.REQUESTED);
    }

    @Transactional
    public void failOnPgRequestError(Long paymentId, Throwable t) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 결제입니다."));

        if (payment.getStatus() == Payment.Status.SUCCESS || payment.getStatus() == Payment.Status.CANCELED) {
            return;
        }

        payment.toFail("t.getMessage()");

        paymentRepository.save(payment);
    }

}
