const parseNumberField = (fieldValue: string): number | null => {
  const trimmedFieldValue = fieldValue.trim();

  if (trimmedFieldValue.length === 0) {
    return null;
  }

  const parsedValue = Number(trimmedFieldValue);
  return Number.isFinite(parsedValue) ? parsedValue : null;
};

export const parseNonNegativeNumberField = (fieldValue: string): number | null => {
  const parsedValue = parseNumberField(fieldValue);
  return parsedValue !== null && parsedValue >= 0 ? parsedValue : null;
};

export const parsePositiveNumberField = (fieldValue: string): number | null => {
  const parsedValue = parseNumberField(fieldValue);
  return parsedValue !== null && parsedValue > 0 ? parsedValue : null;
};
