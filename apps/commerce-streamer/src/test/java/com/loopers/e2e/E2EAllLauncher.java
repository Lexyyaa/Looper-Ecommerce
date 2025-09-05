package com.loopers.e2e;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * - com.loopers.* 전체를 스캔해 commerce-api + commerce-streamer 빈을 한 컨텍스트로 올린다.
 */
@SpringBootApplication(scanBasePackages = "com.loopers")
public class E2EAllLauncher { }
