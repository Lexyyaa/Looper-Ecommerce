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

    @Version
    private Long version;

    public enum Status {
        ACTIVE,
        INACTIVE,
        SOLD_OUT
    }

    public static ProductSku create(Product product, String sku, int price, int stockTotal, int stockReserved) {
        return ProductSku.builder()
                .product(product)
                .sku(sku)
                .status(Status.ACTIVE)
                .price(price)
                .stockTotal(stockTotal)
                .stockReserved(stockReserved)
                .build();
    }

    public int availableQuantity() {
        return (stockTotal - stockReserved);
    }

    public void reserveStock(int quantity) {
        if (quantity < 1) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 수량은 1 이상이어야 합니다.");
        }
        if (availableQuantity() < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        stockTotal -= quantity;
        stockReserved += quantity;
    }

    public void rollbackStock(int quantity) {
        if(stockReserved < quantity){
            throw new CoreException(ErrorType.BAD_REQUEST, "요청재고는 선점재고를 초과할 수 없습니다.");
        }
        stockTotal += quantity;
        stockReserved -= quantity;
    }

    public void confirmStock(int quantity) {
        if(stockReserved < quantity){
            throw new CoreException(ErrorType.BAD_REQUEST, "요청재고는 선점재고를 초과할 수 없습니다.");
        }
        stockReserved -= quantity;
    }

    public void rollbackStockIdempotent(int qty) {
        if (stockReserved >= qty) {
            stockTotal += qty;
            stockReserved -= qty;
        }
    }

    public void confirmStockIdempotent(int qty) {
        if (stockReserved >= qty) {
            stockReserved -= qty;
        }
    }

    public boolean isSoldOut() {
        return (stockTotal - stockReserved) <= 0;
    }

    public void changeStatus(ProductSku.Status status) {
        this.status = status;
    }



}

