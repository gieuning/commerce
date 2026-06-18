package com.gieun.commerce.domain.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
  List<@NotNull OptionGroupRequest> optionGroups;

  @Valid
  @NotEmpty
  List<@NotNull OptionCombinationRequest> combinations;
}
