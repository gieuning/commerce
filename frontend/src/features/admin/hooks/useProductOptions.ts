import { useState } from "react";
import { MESSAGES } from "@/constants/messages";
import type { OptionCombinationRequest, OptionGroupRequest, ProductCreateRequest } from "@/types/product";

export type ProductOptionMode = "single" | "option";

export interface EditableOptionGroup {
  id: string;
  name: string;
  valuesText: string;
}

export interface EditableOptionCombination {
  id: string;
  optionValues: string[];
  additionalPrice: string;
  stock: string;
}

interface ProductOptionState {
  combinations: EditableOptionCombination[];
  errorMessage: string | null;
  groups: EditableOptionGroup[];
  mode: ProductOptionMode;
}

const createOptionGroup = (): EditableOptionGroup => ({
  id: crypto.randomUUID(),
  name: "",
  valuesText: "",
});

const parseOptionValues = (valuesText: string): string[] =>
  valuesText
    .split(",")
    .map((value) => value.trim())
    .filter((value) => value.length > 0);

const parseNumberField = (fieldValue: string): number | null => {
  const parsedValue = Number(fieldValue);
  return Number.isFinite(parsedValue) ? parsedValue : null;
};

const createOptionGroupRequests = (groups: EditableOptionGroup[]): OptionGroupRequest[] | null => {
  const optionGroups = groups.map((group) => ({
    name: group.name.trim(),
    values: parseOptionValues(group.valuesText),
  }));

  return optionGroups.every((group) => group.name.length > 0 && group.values.length > 0)
    ? optionGroups
    : null;
};

const createCombinationRequests = (
  combinations: EditableOptionCombination[],
): OptionCombinationRequest[] | null => {
  const optionCombinations: OptionCombinationRequest[] = [];

  for (const combination of combinations) {
    const additionalPrice = parseNumberField(combination.additionalPrice);
    const stock = parseNumberField(combination.stock);

    if (additionalPrice === null || stock === null) {
      return null;
    }

    optionCombinations.push({ additionalPrice, optionValues: combination.optionValues, stock });
  }

  return optionCombinations;
};

const createOptionValueSets = (groups: EditableOptionGroup[]): string[][] | null => {
  const valueSets = groups.map((group) => parseOptionValues(group.valuesText));
  return valueSets.every((values) => values.length > 0) ? valueSets : null;
};

const createCartesianProduct = (valueSets: string[][]): string[][] =>
  valueSets.reduce<string[][]>(
    (combinations, values) =>
      combinations.flatMap((combination) => values.map((value) => [...combination, value])),
    [[]],
  );

const createEditableCombination = (optionValues: string[]): EditableOptionCombination => ({
  additionalPrice: "0",
  id: optionValues.join("|"),
  optionValues,
  stock: "0",
});

export const useProductOptions = () => {
  const [optionState, setOptionState] = useState<ProductOptionState>({
    combinations: [],
    errorMessage: null,
    groups: [createOptionGroup()],
    mode: "single",
  });

  const setMode = (mode: ProductOptionMode) => {
    setOptionState((currentState) => ({ ...currentState, errorMessage: null, mode }));
  };

  const addGroup = () => {
    setOptionState((currentState) => ({
      ...currentState,
      combinations: [],
      groups: [...currentState.groups, createOptionGroup()],
    }));
  };

  const removeGroup = (groupId: string) => {
    setOptionState((currentState) => ({
      ...currentState,
      combinations: [],
      groups: currentState.groups.filter((group) => group.id !== groupId),
    }));
  };

  const updateGroup = (groupId: string, fieldName: "name" | "valuesText", value: string) => {
    setOptionState((currentState) => ({
      ...currentState,
      combinations: [],
      groups: currentState.groups.map((group) =>
        group.id === groupId ? { ...group, [fieldName]: value } : group,
      ),
    }));
  };

  const generateCombinations = () => {
    const valueSets = createOptionValueSets(optionState.groups);

    if (!valueSets) {
      setOptionState((currentState) => ({
        ...currentState,
        errorMessage: MESSAGES.ADMIN_PRODUCT.OPTION_GROUP_REQUIRED,
      }));
      return;
    }

    setOptionState((currentState) => ({
      ...currentState,
      combinations: createCartesianProduct(valueSets).map(createEditableCombination),
      errorMessage: null,
    }));
  };

  const updateCombination = (
    combinationId: string,
    fieldName: "additionalPrice" | "stock",
    value: string,
  ) => {
    setOptionState((currentState) => ({
      ...currentState,
      combinations: currentState.combinations.map((combination) =>
        combination.id === combinationId ? { ...combination, [fieldName]: value } : combination,
      ),
    }));
  };

  const createOptionProductRequest = (
    baseRequest: Omit<ProductCreateRequest, "combinations" | "optionGroups" | "stock">,
  ): ProductCreateRequest | null => {
    const optionGroups = createOptionGroupRequests(optionState.groups);
    const combinations = createCombinationRequests(optionState.combinations);

    if (!optionGroups) {
      setOptionState((currentState) => ({
        ...currentState,
        errorMessage: MESSAGES.ADMIN_PRODUCT.OPTION_GROUP_REQUIRED,
      }));
      return null;
    }

    if (!combinations || combinations.length === 0) {
      setOptionState((currentState) => ({
        ...currentState,
        errorMessage: MESSAGES.ADMIN_PRODUCT.OPTION_COMBINATION_REQUIRED,
      }));
      return null;
    }

    return { ...baseRequest, combinations, optionGroups };
  };

  return {
    ...optionState,
    addGroup,
    createOptionProductRequest,
    generateCombinations,
    removeGroup,
    setMode,
    updateCombination,
    updateGroup,
  };
};
