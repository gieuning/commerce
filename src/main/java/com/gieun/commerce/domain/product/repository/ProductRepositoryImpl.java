package com.gieun.commerce.domain.product.repository;

import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import com.gieun.commerce.domain.product.entity.QProduct;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

  private static final QProduct product = QProduct.product;

  private final JPAQueryFactory queryFactory;


  @Override
  public Page<Product> search(String keyword, Pageable pageable) {
    List<Product> content = queryFactory
        .selectFrom(product)
        .where(searchConditions(keyword))
        .offset(pageable.getOffset())     // 건너뛸 개수 (page*size 자동)
        .limit(pageable.getPageSize())
        .orderBy(product.createdAt.desc())
        .fetch();// List<Product> 반환

    JPAQuery<Long> countQuery = queryFactory
        .select(product.count())
        .from(product)
        .where(searchConditions(keyword));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }


  private BooleanExpression[] searchConditions(String keyword) {
    return new BooleanExpression[] { notDiscontinued(), keywordMatches(keyword) };
  }

  private BooleanExpression notDiscontinued() {
    return product.status.ne(ProductStatus.STOP_SALE);
  }

  private BooleanExpression keywordMatches(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }
    return product.name.contains(keyword)
        .or(product.description.contains(keyword));
  }
}
