package com.gieun.commerce.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // @Scheduled 활성화 (결제 보상 환불 재시도 스케줄러 등)
public class SchedulingConfig {

}
