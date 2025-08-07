package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_sku")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductSku extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "price", nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductSku.Status status;

    @Column(name = "stock_total", nullable = false)
    private int stockTotal;

    @Column(name = "stock_reserved", nullable = false)
    private int stockReserved;

    public enum Status {
        ACTIVE,
        INACTIVE,
        SOLD_OUT
    }

    public static ProductSku create(Product product, String sku, int price, int stockTotal, int stockReserved) {
        return ProductSku.builder()
                .product(product)
                .sku(sku)
                .price(price)
                .stockTotal(stockTotal)
                .stockReserved(stockReserved)
                .build();
    }

    public boolean isSoldOut() {
        return (stockTotal - stockReserved) <= 0;
    }

    public int avaliableQunatity() {
        return (stockTotal - stockReserved);
    }

    public void reserveStock(int quantity) {
        if (avaliableQunatity() < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        stockReserved += quantity;
    }

    public void rollbackStock(int quantity) {
        if(stockReserved < quantity){
            throw new CoreException(ErrorType.BAD_REQUEST, "요청재고는 선점재고를 초과할 수 없습니다.");
        }
        stockReserved -= quantity;
    }
}

