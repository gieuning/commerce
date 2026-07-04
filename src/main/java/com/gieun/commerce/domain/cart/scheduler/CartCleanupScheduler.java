package com.gieun.commerce.domain.cart.scheduler;

import com.gieun.commerce.domain.cart.service.CartService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 방치된 게스트 장바구니를 주기적으로 정리한다. 게스트 카트는 재고를 예약하지 않으므로(재고는 결제 승인 시 차감)
 * 순수 DB 위생 목적이며, 생성 후 TTL_DAYS가 지난 게스트 카트를 삭제한다. (회원 카트는 대상 아님)
 */
@Component
@RequiredArgsConstructor
public class CartCleanupScheduler {

  private static final int TTL_DAYS = 30;
  private static final int BATCH_SIZE = 500;
  private static final Logger log = LoggerFactory.getLogger(CartCleanupScheduler.class);

  private final CartService cartService;

  @Scheduled(cron = "${cart.guest.cleanup-cron:0 0 4 * * *}")
  public void cleanupAbandonedGuestCarts() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(TTL_DAYS);

    int totalDeleted = 0;
    int deleted;
    do {
      deleted = cartService.deleteAbandonedGuestCarts(cutoff, BATCH_SIZE);
      totalDeleted += deleted;
    } while (deleted == BATCH_SIZE);

    if (totalDeleted > 0) {
      log.info("방치 게스트 카트 정리 완료 — cutoff={}, deleted={}", cutoff, totalDeleted);
    }
  }
}
