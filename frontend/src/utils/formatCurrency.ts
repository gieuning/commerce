const WON_FORMATTER = new Intl.NumberFormat("ko-KR", {
  maximumFractionDigits: 0,
});

export const formatCurrency = (amount: number | string): string => {
  const numericAmount = typeof amount === "string" ? Number(amount) : amount;

  if (!Number.isFinite(numericAmount)) {
    return "-";
  }

  return `${WON_FORMATTER.format(Math.round(numericAmount))}원`;
};
