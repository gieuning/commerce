package com.gieun.commerce.domain.order.repository;

import static com.gieun.commerce.domain.order.entity.QOrder.order;
import static com.gieun.commerce.domain.order.entity.QOrderItem.orderItem;
import static com.gieun.commerce.global.exception.DomainExceptionCode.INVALID_SORT_PROPERTY;

import com.gieun.commerce.domain.order.entity.Order;
import com.gieun.commerce.domain.order.repository.condition.OrderSearchCondition;
import com.gieun.commerce.global.exception.DomainException;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Order> search(Long userId, OrderSearchCondition condition, Pageable pageable) {
    OrderSpecifier<?> orderSpecifier = orderSpecifier(pageable);

    List<Long> orderIds = queryFactory
        .select(order.id)
        .from(order)
        .where(
            order.userId.eq(userId),
            productNameContains(condition.getProductName()),
            statusEq(condition),
            orderedAtFrom(condition),
            orderedAtTo(condition)
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(orderSpecifier, order.id.desc())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(order.count())
        .from(order)
        .where(
            order.userId.eq(userId),
            productNameContains(condition.getProductName()),
            statusEq(condition),
            orderedAtFrom(condition),
            orderedAtTo(condition)
        );

    if (orderIds.isEmpty()) {
      return PageableExecutionUtils.getPage(List.of(), pageable, countQuery::fetchOne);
    }

    List<Order> content = queryFactory
        .selectFrom(order)
        .distinct()
        .leftJoin(order.items, orderItem).fetchJoin()
        .where(order.id.in(orderIds))
        .orderBy(orderSpecifier, order.id.desc())
        .fetch();

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private OrderSpecifier<?> orderSpecifier(Pageable pageable) {
    if (pageable.getSort().isUnsorted()) {
      return order.orderedAt.desc();
    }

    Sort.Order sortOrder = pageable.getSort().iterator().next();

    if ("orderedAt".equals(sortOrder.getProperty())) {
      return sortOrder.isAscending() ? order.orderedAt.asc() : order.orderedAt.desc();
    }
    if ("totalPrice".equals(sortOrder.getProperty())) {
      return sortOrder.isAscending() ? order.totalPrice.asc() : order.totalPrice.desc();
    }

    throw new DomainException(INVALID_SORT_PROPERTY);
  }

  private BooleanExpression productNameContains(String productName) {
    if (!StringUtils.hasText(productName)) {
      return null;
    }
    return JPAExpressions
        .selectOne()
        .from(orderItem)
        .where(
            orderItem.order.eq(order),
            orderItem.productName.contains(productName)
        )
        .exists();
  }

  private BooleanExpression statusEq(OrderSearchCondition condition) {
    if (condition.getStatus() == null) {
      return null;
    }
    return order.status.eq(condition.getStatus());
  }

  private BooleanExpression orderedAtFrom(OrderSearchCondition condition) {
    if (condition.getFrom() == null) {
      return null;
    }
    return order.orderedAt.goe(condition.getFrom().atStartOfDay());
  }

  private BooleanExpression orderedAtTo(OrderSearchCondition condition) {
    if (condition.getTo() == null) {
      return null;
    }
    return order.orderedAt.loe(condition.getTo().atTime(LocalTime.MAX));
  }
}
