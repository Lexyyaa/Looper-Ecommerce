package com.loopers.domain.ranking;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name="product_metrics_monthly")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMetricsMonthly {

    @EmbeddedId
    private ProductMetricsMonthlyId id;

    @Column(nullable=false)
    private double score;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    public static ProductMetricsMonthly of(String yearMonth, Long productId, double score){
        return ProductMetricsMonthly.builder()
                .id(new ProductMetricsMonthlyId(yearMonth, productId))
                .score(score)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public record YearMonth(String value){
        public static YearMonth of(LocalDate d){
            return new YearMonth(DateTimeFormatter.ofPattern("yyyyMM").format(d));
        }
        public YearMonth prev(){
            LocalDate first = LocalDate.parse(this.value() + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
            return of(first.minusMonths(1));
        }
    }
}
