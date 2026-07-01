import { MESSAGES } from "@/constants/messages";
import { cn } from "@/utils/cn";

interface ProductImageProps {
  imageUrl: string | null;
  name: string;
  soldOut?: boolean;
  className?: string;
}

export const ProductImage = ({ imageUrl, name, soldOut = false, className }: ProductImageProps) => (
  <div className={cn("relative aspect-[4/3] bg-line", className)}>
    {imageUrl ? (
      <img alt={name} className="h-full w-full object-cover" src={imageUrl} />
    ) : (
      <div className="flex h-full items-center justify-center text-sm text-neutral">
        {MESSAGES.PRODUCT.IMAGE_PLACEHOLDER}
      </div>
    )}
    {soldOut ? (
      <div className="absolute inset-0 flex items-center justify-center bg-black/50">
        <span className="rounded-full border-2 border-white px-4 py-1 text-sm font-bold uppercase tracking-widest text-white">
          {MESSAGES.PRODUCT.SOLD_OUT}
        </span>
      </div>
    ) : null}
  </div>
);
