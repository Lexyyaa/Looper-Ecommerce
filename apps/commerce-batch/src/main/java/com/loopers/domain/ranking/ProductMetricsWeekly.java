package com.loopers.domain.ranking;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

@Entity
@Table(name="product_metrics_weekly")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMetricsWeekly {

    @EmbeddedId
    private ProductMetricsWeeklyId id;

    @Column(nullable=false)
    private double score;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    public static ProductMetricsWeekly of(String yearWeek, Long productId, double score){
        return ProductMetricsWeekly.builder()
                .id(new ProductMetricsWeeklyId(yearWeek, productId))
                .score(score)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public record YearWeek(String value) {
        public static YearWeek of(LocalDate date){
            WeekFields wf = WeekFields.ISO;
            int w = date.get(wf.weekOfWeekBasedYear());
            int y = date.get(wf.weekBasedYear());
            return new YearWeek("%04dW%02d".formatted(y, w));
        }
        public static LocalDate start(LocalDate d){ return d.with(WeekFields.ISO.dayOfWeek(), 1); }
        public static LocalDate end(LocalDate d){ return d.with(WeekFields.ISO.dayOfWeek(), 7); }

        public YearWeek prev(){
            int year = Integer.parseInt(value().substring(0, 4));
            int week = Integer.parseInt(value().substring(5));
            LocalDate firstThursday = LocalDate.of(year, 1, 4);
            LocalDate mondayOfWeek = firstThursday
                    .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                    .with(WeekFields.ISO.dayOfWeek(), 1);
            return of(mondayOfWeek.minusWeeks(1));
        }
    }
}
