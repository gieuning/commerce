package com.gieun.commerce.domain.product.service;

import com.gieun.commerce.domain.product.dto.request.OptionCombinationRequest;
import com.gieun.commerce.domain.product.dto.request.OptionCombinationUpdateRequest;
import com.gieun.commerce.domain.product.dto.request.OptionGroupRequest;
import com.gieun.commerce.domain.product.dto.request.OptionReplaceRequest;
import com.gieun.commerce.domain.product.dto.request.ProductCreateRequest;
import com.gieun.commerce.domain.product.dto.request.ProductUpdateRequest;
import com.gieun.commerce.domain.product.dto.request.StockUpdateRequest;
import com.gieun.commerce.domain.product.dto.response.ProductDetailResponse;
import com.gieun.commerce.domain.product.dto.response.ProductResponse;
import com.gieun.commerce.domain.product.entity.CombinationValue;
import com.gieun.commerce.domain.product.entity.OptionCombination;
import com.gieun.commerce.domain.product.entity.OptionGroup;
import com.gieun.commerce.domain.product.entity.OptionValue;
import com.gieun.commerce.domain.product.entity.Product;
import com.gieun.commerce.domain.product.repository.OptionCombinationRepository;
import com.gieun.commerce.domain.product.repository.OptionGroupRepository;
import com.gieun.commerce.domain.product.repository.ProductRepository;
import com.gieun.commerce.global.exception.DomainException;
import com.gieun.commerce.global.exception.DomainExceptionCode;
import com.gieun.commerce.global.response.PageResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

  private final ProductRepository productRepository;
  private final OptionGroupRepository optionGroupRepository;
  private final OptionCombinationRepository optionCombinationRepository;

  @Transactional
  public ProductDetailResponse create(ProductCreateRequest request) {
    boolean hasOptions = request.getOptionGroups() != null && !request.getOptionGroups().isEmpty();
    validateOptionRequest(request, hasOptions);

    Product product = Product.create(
        request.getName(),
        request.getDescription(),
        request.getPrice(),
        hasOptions ? 0 : request.getStock(),
        request.getImageUrl()
    );

    if (hasOptions) {
      List<OptionGroup> groups = buildOptionGroups(product, request.getOptionGroups());
      buildCombinations(product, groups, request.getCombinations());
    }


    return ProductDetailResponse.from(productRepository.save(product));
  }

  private void validateOptionRequest(ProductCreateRequest request, boolean hasOptions) {
    boolean hasCombinations =
        request.getCombinations() != null && !request.getCombinations().isEmpty();

    if (hasOptions) {
      if (!hasCombinations || request.getStock() != null) {
        throw new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST);
      }
      return;
    }
    if (hasCombinations || request.getStock() == null) {
      throw new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST);
    }
  }

  private void validateReplaceRequest(OptionReplaceRequest request) {
    boolean hasOptionGroups =
        request.getOptionGroups() != null && !request.getOptionGroups().isEmpty();
    boolean hasCombinations =
        request.getCombinations() != null && !request.getCombinations().isEmpty();
    if (!hasOptionGroups || !hasCombinations) {
      throw new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST);
    }
  }

  private List<OptionGroup> buildOptionGroups(Product product, List<OptionGroupRequest> requests) {
    Set<String> groupNames = new HashSet<>();
    for (int i = 0; i < requests.size(); i++) {
      OptionGroupRequest groupRequest = requests.get(i);
      if (!groupNames.add(groupRequest.getName())) {
        throw new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST);
      }
      OptionGroup group = OptionGroup.create(groupRequest.getName(), i);
      List<String> values = groupRequest.getValues();
      Set<String> valueNames = new HashSet<>();
      for (int j = 0; j < values.size(); j++) {
        if (!valueNames.add(values.get(j))) {
          throw new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST);
        }
        group.addValue(OptionValue.create(values.get(j), j));
      }
      product.addOptionGroup(group);
    }
    return product.getOptionGroups();
  }

  private void buildCombinations(Product product, List<OptionGroup> groups,
      List<OptionCombinationRequest> requests) {
    Set<List<String>> seen = new HashSet<>();
    for (OptionCombinationRequest combinationRequest : requests) {
      List<String> valueNames = combinationRequest.getOptionValues();
      if (valueNames.size() != groups.size()) {
        throw new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST);
      }

      if (!seen.add(List.copyOf(valueNames))) {
        throw new DomainException(DomainExceptionCode.DUPLICATE_OPTION_COMBINATION);
      }
      OptionCombination combination = OptionCombination.create(
          combinationRequest.getAdditionalPrice(), combinationRequest.getStock()
      );
      for (int i = 0; i < groups.size(); i++) {
        OptionGroup group = groups.get(i);
        String valueName = valueNames.get(i);

        OptionValue value = group.getValues().stream()
            .filter(optionValue -> optionValue.getName().equals(valueName))
            .findFirst()
            .orElseThrow(() -> new DomainException(DomainExceptionCode.INVALID_OPTION_REQUEST));

        combination.addValue(CombinationValue.create(group, value));
      }
      product.addOptionCombination(combination);
    }
  }


  public PageResult<ProductResponse> getList(String keyword, Pageable pageable) {
    return new PageResult<>(
        productRepository.search(keyword, pageable).map(ProductResponse::from));
  }

  public ProductDetailResponse getDetail(Long id) {
    return ProductDetailResponse.from(findDetailProduct(id));
  }

  @Transactional
  public ProductDetailResponse update(Long id, ProductUpdateRequest request) {
    Product product = findProductForUpdate(id);
    product.update(request.getName(), request.getDescription(), request.getPrice(),
        request.getImageUrl());
    return ProductDetailResponse.from(product);
  }

  @Transactional
  public ProductDetailResponse updateStock(Long id, StockUpdateRequest request) {
    Product product = findProductForUpdate(id);
    if (optionGroupRepository.existsByProductId(id)) {
      throw new DomainException(DomainExceptionCode.PRODUCT_HAS_OPTIONS);
    }
    product.updateStock(request.getStock());
    return ProductDetailResponse.from(product);
  }

  @Transactional
  public ProductDetailResponse updateCombination(Long id, Long combinationId,
      OptionCombinationUpdateRequest request) {
    Product product = findProductForUpdate(id);
    OptionCombination combination = optionCombinationRepository
        .findByIdAndProductIdForUpdate(combinationId, id)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_OPTION_COMBINATION));
    combination.update(request.getAdditionalPrice(), request.getStock(), request.getStatus());
    return ProductDetailResponse.from(product);
  }

  @Transactional
  public ProductDetailResponse replaceOptions(Long id, OptionReplaceRequest request) {
    validateReplaceRequest(request);
    Product product = findProductForUpdate(id);
    product.clearOptions();
    product.updateStock(0);
    List<OptionGroup> groups = buildOptionGroups(product, request.getOptionGroups());
    buildCombinations(product, groups, request.getCombinations());
    productRepository.flush();
    return ProductDetailResponse.from(product);
  }

  @Transactional
  public void delete(Long id) {
    findProductForUpdate(id).discontinue();
  }

  private Product findProductForUpdate(Long id) {
    return productRepository.findByIdForUpdate(id)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));
  }

  private Product findDetailProduct(Long id) {
    return productRepository.findDetailById(id)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));
  }
}
