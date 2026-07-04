import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppLayout } from "@/components/AppLayout";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { ROUTES } from "@/constants/routes";
import { AdminProductFormPage } from "@/features/admin/AdminProductFormPage";
import { AdminProductListPage } from "@/features/admin/AdminProductListPage";
import { LoginPage } from "@/features/auth/LoginPage";
import { SignupPage } from "@/features/auth/SignupPage";
import { CartPage } from "@/features/cart/CartPage";
import { OrderDetailPage } from "@/features/orders/OrderDetailPage";
import { OrderListPage } from "@/features/orders/OrderListPage";
import { PaymentCheckoutPage } from "@/features/payments/PaymentCheckoutPage";
import { PaymentFailPage } from "@/features/payments/PaymentFailPage";
import { PaymentSuccessPage } from "@/features/payments/PaymentSuccessPage";
import { ProductDetailPage } from "@/features/products/ProductDetailPage";
import { ProductListPage } from "@/features/products/ProductListPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      {
        index: true,
        element: <Navigate replace to={ROUTES.PRODUCTS} />,
      },
      {
        path: "products",
        element: <ProductListPage />,
      },
      {
        path: "products/:productId",
        element: <ProductDetailPage />,
      },
      {
        path: "login",
        element: <LoginPage />,
      },
      {
        path: "signup",
        element: <SignupPage />,
      },
      {
        // 장바구니는 게스트도 접근 가능 (담기/조회). 결제는 아래 ProtectedRoute에서 인증 필요.
        path: "cart",
        element: <CartPage />,
      },
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: "orders",
            element: <OrderListPage />,
          },
          {
            path: "orders/:orderId",
            element: <OrderDetailPage />,
          },
          {
            path: "payments/checkout/:orderId",
            element: <PaymentCheckoutPage />,
          },
          {
            path: "payments/success",
            element: <PaymentSuccessPage />,
          },
          {
            path: "payments/fail",
            element: <PaymentFailPage />,
          },
        ],
      },
      {
        element: <ProtectedRoute requireAdmin />,
        children: [
          {
            path: "admin/products",
            element: <AdminProductListPage />,
          },
          {
            path: "admin/products/new",
            element: <AdminProductFormPage />,
          },
          {
            path: "admin/products/:productId/edit",
            element: <AdminProductFormPage />,
          },
        ],
      },
    ],
  },
]);
