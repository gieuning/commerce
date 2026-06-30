import { Link } from "react-router-dom";
import { PriceText } from "@/components/PriceText";
import { StatusBadge } from "@/components/StatusBadge";
import { MESSAGES } from "@/constants/messages";
import { ROUTES } from "@/constants/routes";
import { PRODUCT_STATUS_LABELS, PRODUCT_STATUS_TONES } from "@/constants/statusLabels";
import type { ProductSummary } from "@/types/product";

interface ProductCardProps {
  product: ProductSummary;
}

export const ProductCard = ({ product }: ProductCardProps) => (
  <Link
    className="group overflow-hidden rounded-card border border-line bg-surface transition hover:border-primary"
    to={ROUTES.PRODUCT_DETAIL(product.id)}
  >
    <div className="aspect-[4/3] bg-line">
      {product.imageUrl ? (
        <img
          alt={product.name}
          className="h-full w-full object-cover"
          src={product.imageUrl}
        />
      ) : (
        <div className="flex h-full items-center justify-center text-sm text-neutral">
          {MESSAGES.PRODUCT.IMAGE_PLACEHOLDER}
        </div>
      )}
    </div>
    <div className="grid gap-3 p-4">
      <div className="flex items-start justify-between gap-3">
        <h2 className="line-clamp-2 text-base font-semibold text-ink group-hover:text-primary">
          {product.name}
        </h2>
        <StatusBadge
          label={PRODUCT_STATUS_LABELS[product.status]}
          tone={PRODUCT_STATUS_TONES[product.status]}
        />
      </div>
      <p className="line-clamp-2 min-h-10 text-sm leading-5 text-ink-soft">
        {product.description ?? "상품 설명이 없습니다."}
      </p>
      <PriceText amount={product.price} className="text-lg font-bold text-ink" />
    </div>
  </Link>
);
