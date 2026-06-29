type ClassValue = string | false | null | undefined;

export const cn = (...classValues: ClassValue[]): string =>
  classValues.filter(Boolean).join(" ");
