package com.loopers.infrastructure.pg;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.domain.paymentgateway.PaymentGatewayRequest;
import com.loopers.domain.paymentgateway.PaymentGatewayResponse;
import com.loopers.domain.pg.PgClientAdapter;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgClientAdapterImpl implements PgClientAdapter {

    @Value("${pg.userid}")
    private String pgUserId;

    private final PgFeignClient client;

    @Override
    public PaymentGatewayResponse acceptPayment(PaymentGatewayRequest req) {
        PgFeignDto.PgPayRequest body = new PgFeignDto.PgPayRequest(
                req.orderId(),
                req.cardType(),
                req.cardNo(),
                req.amount(),
                req.callbackUrl(),
                req.idempotencyKey()
        );
        PgFeignDto.PgPayResponse res = client.pay(pgUserId, body);

        return PgFeignDto.PgPayResponse.toResponse(res);
    }

    @Override
    public PaymentDetail getByTxKey(String txKey) {
        PgFeignDto.PgPayDetail res = client.get(pgUserId, txKey);
        return PgFeignDto.PgPayDetail.toResponse(res);
    }

    @Override
    @Retry(name = "pg-read", fallbackMethod = "returnEmptyObject")
    public Optional<PaymentDetail> findSingleSuccessPayment(String orderId) {
        PgFeignDto.PgTxList list = client.byOrder(pgUserId, orderId);

        List<PgFeignDto.PgTxList.Data.Tx> txs = (list != null && list.data() != null && list.data().transactions() != null)
                ? list.data().transactions()
                : List.<PgFeignDto.PgTxList.Data.Tx>of();

        List<String> successTxKeys = txs.stream()
                .filter(tx -> {
                    String s = String.valueOf(tx.status());
                    return s != null && s.equals(Payment.Status.SUCCESS.name());
                })
                .map(PgFeignDto.PgTxList.Data.Tx::transactionKey)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (successTxKeys.isEmpty()) {
            return Optional.empty();
        }
        if (successTxKeys.size() > 1) {
            throw new CoreException(ErrorType.CONFLICT,
                    "중복 성공처리된 건 : orderId=" + orderId);
        }

        String txKey = successTxKeys.get(0);
        return Optional.of(getByTxKey(txKey));
    }

    protected Optional<PaymentDetail> returnEmptyObject(String orderId, Throwable t) {
        log.warn("pg-read 실패 orderId : {} ", orderId, t.getMessage());
        return Optional.empty();
    }
}
