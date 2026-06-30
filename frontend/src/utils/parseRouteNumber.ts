export const parseRouteNumber = (routeValue: string | undefined): number | null => {
  if (!routeValue || !/^\d+$/.test(routeValue)) {
    return null;
  }

  return Number(routeValue);
};
