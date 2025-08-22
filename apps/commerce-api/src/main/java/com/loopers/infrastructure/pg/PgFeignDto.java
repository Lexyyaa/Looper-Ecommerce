package com.loopers.infrastructure.pg;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.paymentgateway.PaymentDetail;
import com.loopers.domain.paymentgateway.PaymentGatewayResponse;

public class PgFeignDto {

    public record PgPayRequest(String orderId, String cardType, String cardNo,
                        String amount, String callbackUrl, String idempotencyKey) {}

    public record PgPayResponse(Meta meta, Data data) {

        public record Meta(String result) {}

        public record Data(String transactionKey, String status, String reason) {
        }
        public static PaymentGatewayResponse toResponse(PgPayResponse res) {
            if (res == null) return new PaymentGatewayResponse(null, null, null);
            Meta m = res.meta();
            Data d = res.data();
            if (d == null) {
                return new PaymentGatewayResponse(null, null, (m != null ? m.result() : null));
            }
            return new PaymentGatewayResponse(d.transactionKey(), d.status(), d.reason());
        }
    }

    public record PgPayDetail(Meta meta, Data data) {

        public record Meta(String result) {}

        public record Data(
                String transactionKey,
                String orderId,
                String cardType,
                String cardNo,
                Long amount,
                Payment.Status status,
                String reason
        ) {}
        public static PaymentDetail toResponse(PgPayDetail res) {
            if (res == null) return new PaymentDetail(null, null, null, null, null);
            Meta m = res.meta();
            Data d = res.data();
            if (d == null) {
                return new PaymentDetail(null, null, null, null, (m != null ? m.result() : null));
            }
            return new PaymentDetail(
                    d.transactionKey(),
                    d.orderId(),
                    d.amount(),
                    d.status(),
                    d.reason()
            );
        }
    }
    record PgTxList(Meta meta, Data data) {
        record Meta(String result) {}
        record Data(String orderId, java.util.List<Tx> transactions) {
            record Tx(String transactionKey, Payment.Status status, String reason) {}
        }
    }
}
