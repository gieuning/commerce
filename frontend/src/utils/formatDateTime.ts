const padDatePart = (value: number): string => String(value).padStart(2, "0");

export const formatDateTime = (dateTime: string | null | undefined): string => {
  if (!dateTime) {
    return "-";
  }

  const parsedDate = new Date(dateTime);

  if (Number.isNaN(parsedDate.getTime())) {
    return "-";
  }

  const year = parsedDate.getFullYear();
  const month = padDatePart(parsedDate.getMonth() + 1);
  const day = padDatePart(parsedDate.getDate());
  const hours = padDatePart(parsedDate.getHours());
  const minutes = padDatePart(parsedDate.getMinutes());

  return `${year}. ${month}. ${day}. ${hours}:${minutes}`;
};
