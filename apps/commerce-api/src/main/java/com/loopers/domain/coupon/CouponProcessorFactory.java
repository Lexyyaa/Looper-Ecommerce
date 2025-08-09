package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CouponProcessorFactory {

    private final Map<Coupon.TargetType, CouponProcessor> processorMap;

    public CouponProcessorFactory(List<CouponProcessor> processors) {
        this.processorMap = processors.stream()
                .collect(Collectors.toMap(CouponProcessor::getTargetType, processor -> processor));
    }

    public CouponProcessor getProcessor(Coupon.TargetType targetType) {
        CouponProcessor processor = processorMap.get(targetType);
        if (processor == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 쿠폰 타입을 지원하는 Processor가 없습니다.");
        }
        return processor;
    }
}
