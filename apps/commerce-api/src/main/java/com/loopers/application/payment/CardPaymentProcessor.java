package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.pg.PgClientAdapter;
import com.loopers.domain.user.User;
import com.loopers.domain.paymentgateway.PaymentGatewayRequest;
import com.loopers.domain.paymentgateway.PaymentGatewayResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PgClientAdapter pgClientAdapter;
    private final PaymentService paymentService;

    @Value("${pg.callback.base-url}")
    private String pgCallbackBaseUrl;

    @Value("${pg.callback.transaction-path}")
    private String pgCallbackTransactionPath;

    @Value("${pg.userid}")
    private String pgUserId;

    @Override
    public Payment.Method getMethod() {
        return Payment.Method.CARD;
    }

    @Retry(name = "pg-request")
    @RateLimiter(name = "pg-request", fallbackMethod = "fallbackPending")
    @CircuitBreaker(name = "pg-request", fallbackMethod = "fallbackPending")
    public Payment pay(User user, Order order,PaymentCommand.CreatePayment command) {

        Payment payment = paymentService.createPending(
                user.getId(),
                order.getId(),
                order.getFinalPrice(),
                command.method()
        );

        String callbackUrl = pgCallbackBaseUrl + pgCallbackTransactionPath;

        PaymentGatewayResponse res = requestToPg(
                payment.getId(),
                new PaymentGatewayRequest(
                        pgUserId + payment.getOrderId(),
                        command.details().cardType(),
                        command.details().cardNo(),
                        payment.getAmount().toString(),
                        callbackUrl,
                        payment.getIdempotencyKey()
                )
        );
        Payment requested = null;
        if (res.txKey() != null) {
            requested = paymentService.bindTxKeyOnPending(payment, res.txKey());
            return requested;
        }else{
            return payment;
        }
    }

    @Retry(name = "pg-request")
    @RateLimiter(name = "pg-request")
    @CircuitBreaker(name = "pg-request", fallbackMethod = "fallbackPending")
    protected PaymentGatewayResponse requestToPg(Long paymentId, PaymentGatewayRequest req) {
        return pgClientAdapter.acceptPayment(req);
    }

    protected PaymentGatewayResponse fallbackPending(Long paymentId,
                                                     PaymentGatewayRequest req,
                                                     Throwable t) {
        paymentService.failOnPgRequestError(paymentId, t);
        return new PaymentGatewayResponse(null, null,
                "PG request failed: " + (t != null ? t.getMessage() : null));
    }
}
