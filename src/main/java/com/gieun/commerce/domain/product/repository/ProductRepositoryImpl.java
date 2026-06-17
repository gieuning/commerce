package com.gieun.commerce.domain.product.repository;

import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.entity.ProductStatus;
import com.gieun.commerce.domain.product.entity.QCombinationValue;
import com.gieun.commerce.domain.product.entity.QOptionCombination;
import com.gieun.commerce.domain.product.entity.QOptionGroup;
import com.gieun.commerce.domain.product.entity.QOptionValue;
import com.gieun.commerce.domain.product.entity.QProduct;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

  private static final QProduct product = QProduct.product;
  private static final QOptionGroup optionGroup = QOptionGroup.optionGroup;
  private static final QOptionValue optionValue = QOptionValue.optionValue;
  private static final QOptionCombination optionCombination = QOptionCombination.optionCombination;
  private static final QCombinationValue combinationValue = QCombinationValue.combinationValue;

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

  @Override
  public Optional<Product> findDetailById(Long id) {
    Product foundProduct = queryFactory
        .selectFrom(product)
        .distinct()
        .leftJoin(product.optionGroups, optionGroup).fetchJoin()
        .where(product.id.eq(id))
        .fetchOne();

    if (foundProduct == null) {
      return Optional.empty();
    }

    queryFactory
        .selectFrom(product)
        .distinct()
        .leftJoin(product.optionCombinations, optionCombination).fetchJoin()
        .where(product.id.eq(id))
        .fetch();

    queryFactory
        .selectFrom(optionGroup)
        .distinct()
        .leftJoin(optionGroup.values, optionValue).fetchJoin()
        .where(optionGroup.product.id.eq(id))
        .orderBy(optionGroup.sortOrder.asc(), optionValue.sortOrder.asc())
        .fetch();

    queryFactory
        .selectFrom(optionCombination)
        .distinct()
        .leftJoin(optionCombination.values, combinationValue).fetchJoin()
        .leftJoin(combinationValue.optionValue, optionValue).fetchJoin()
        .where(optionCombination.product.id.eq(id))
        .fetch();

    return Optional.of(foundProduct);
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
