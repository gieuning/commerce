package com.gieun.commerce.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling // @Scheduled 활성화 (결제 보상 재시도, 게스트 카트 정리 등)
public class SchedulingConfig {

  // @Scheduled 기본 스케줄러는 단일 스레드라, 한 작업이 오래 돌면 다른 스케줄 작업이 밀린다.
  // (예: 게스트 카트 정리 배치 루프가 결제 보상 스케줄러 실행을 지연) → 전용 스레드 풀로 분리한다.
  // 풀 크기는 동시에 돌 수 있는 @Scheduled 작업 수 이상으로 유지한다.
  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(3);
    scheduler.setThreadNamePrefix("scheduled-task-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(30);
    return scheduler;
  }
}
