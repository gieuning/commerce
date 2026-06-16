package com.gieun.commerce.domain.product.entity;

import com.gieun.commerce.global.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "product_option_combination_values")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CombinationValue extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "combination_id", nullable = false)
  OptionCombination combination;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "option_value_id", nullable = false)
  OptionValue optionValue;

  public static CombinationValue create(OptionValue optionValue) {
    return CombinationValue.builder()
        .optionValue(optionValue)
        .build();
  }

  void assignCombination(OptionCombination combination) {
    this.combination = combination;
  }
}
