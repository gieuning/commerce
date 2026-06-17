package com.gieun.commerce.domain.product.dto.response;

import com.gieun.commerce.domain.product.entity.OptionGroup;
import com.gieun.commerce.domain.product.entity.OptionValue;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionGroupResponse {

  String name;
  List<String> values;

  public static OptionGroupResponse from(OptionGroup group) {
    return OptionGroupResponse.builder()
        .name(group.getName())
        .values(group.getValues().stream().map(OptionValue::getName).toList())
        .build();
  }
}
