import { Navigate, Outlet, useLocation } from "react-router-dom";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { useAuth } from "@/hooks/useAuth";
import { EmptyState } from "@/components/EmptyState";
import { LoadingState } from "@/components/LoadingState";

interface ProtectedRouteProps {
  requireAdmin?: boolean;
}

export const ProtectedRoute = ({ requireAdmin = false }: ProtectedRouteProps) => {
  const { isAdmin, isAuthLoading, isAuthenticated } = useAuth();
  const location = useLocation();

  if (isAuthLoading) {
    return <LoadingState />;
  }

  if (!isAuthenticated) {
    return <Navigate replace state={{ from: location.pathname }} to={ROUTES.LOGIN} />;
  }

  if (requireAdmin && !isAdmin) {
    return <EmptyState message={MESSAGES.AUTH.ADMIN_REQUIRED} />;
  }

  return <Outlet />;
};
