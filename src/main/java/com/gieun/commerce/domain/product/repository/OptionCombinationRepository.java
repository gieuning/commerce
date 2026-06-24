package com.gieun.commerce.domain.product.repository;

import com.gieun.commerce.domain.product.entity.OptionCombination;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OptionCombinationRepository extends JpaRepository<OptionCombination, Long> {

  @Query("""
      select distinct combination
      from OptionCombination combination
      left join fetch combination.values value
      left join fetch value.optionValue
      where combination.id in :ids
      """)
  List<OptionCombination> findAllWithValuesByIdIn(@Param("ids") Collection<Long> ids);


  boolean existsByProductId(Long productId);
  Optional<OptionCombination> findByIdAndProductId(Long id, Long productId);
}
