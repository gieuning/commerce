export const parsePaymentAmount = (amount: string): number => {
  const parsedAmount = Number(amount);

  if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
    throw new Error("Invalid payment amount");
  }

  return parsedAmount;
};
