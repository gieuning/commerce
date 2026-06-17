package com.gieun.commerce.domain.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionReplaceRequest {

  @Valid
  @NotEmpty
  List<OptionGroupRequest> optionGroups;

  @Valid
  @NotEmpty
  List<OptionCombinationRequest> combinations;
}
