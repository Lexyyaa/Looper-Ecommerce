package com.loopers.domain.payment;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 결제입니다."));
    }

    public void validateCancelable(Payment payment, User user) {
        if (!payment.getUserId().equals(user.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST,"본인의 결제만 취소할 수 있습니다.");
        }
        if (payment.getStatus() == Payment.Status.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 취소된 결제입니다.");
        }
    }

    public Payment cancelPayment(Payment currPayment) {
        currPayment.cancel();
        Payment payment = paymentRepository.save(currPayment);
        return payment;
    }
}
