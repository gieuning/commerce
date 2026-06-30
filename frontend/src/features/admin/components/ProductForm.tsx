import { type FormEvent, useState } from "react";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { Select } from "@/components/Select";
import { Textarea } from "@/components/Textarea";
import { MESSAGES } from "@/constants/messages";
import { ProductOptionFields } from "@/features/admin/components/ProductOptionFields";
import { useProductOptions, type ProductOptionMode } from "@/features/admin/hooks/useProductOptions";
import type { ProductCreateRequest, ProductDetail, ProductUpdateRequest } from "@/types/product";

interface ProductFormProps {
  product?: ProductDetail;
  isSubmitting: boolean;
  onSubmit: (requestBody: ProductCreateRequest | ProductUpdateRequest) => void;
}

const parseNumberField = (fieldValue: string): number | null => {
  const parsedValue = Number(fieldValue);
  return Number.isFinite(parsedValue) ? parsedValue : null;
};

export const ProductForm = ({ isSubmitting, onSubmit, product }: ProductFormProps) => {
  const [name, setName] = useState(product?.name ?? "");
  const [description, setDescription] = useState(product?.description ?? "");
  const [price, setPrice] = useState(product ? String(product.price) : "");
  const [stock, setStock] = useState(product ? String(product.stock) : "");
  const [imageUrl, setImageUrl] = useState(product?.imageUrl ?? "");
  const [formErrorMessage, setFormErrorMessage] = useState<string | null>(null);
  const productOptions = useProductOptions();

  const createBaseCreateRequest = (): Omit<ProductCreateRequest, "stock"> | null => {
    const parsedPrice = parseNumberField(price);

    if (parsedPrice === null) {
      setFormErrorMessage(MESSAGES.ADMIN_PRODUCT.INVALID_NUMBER);
      return null;
    }

    return {
      description: description || undefined,
      imageUrl: imageUrl || undefined,
      name,
      price: parsedPrice,
    };
  };

  const createSingleProductRequest = (
    baseRequest: Omit<ProductCreateRequest, "stock">,
  ): ProductCreateRequest | null => {
    const parsedStock = parseNumberField(stock);

    if (parsedStock === null) {
      setFormErrorMessage(MESSAGES.ADMIN_PRODUCT.INVALID_NUMBER);
      return null;
    }

    return { ...baseRequest, stock: parsedStock };
  };

  const createCreateRequest = (): ProductCreateRequest | null => {
    const baseRequest = createBaseCreateRequest();

    if (!baseRequest) {
      return null;
    }

    return productOptions.mode === "option"
      ? productOptions.createOptionProductRequest(baseRequest)
      : createSingleProductRequest(baseRequest);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFormErrorMessage(null);

    if (product) {
      onSubmit({ description: description || undefined, imageUrl: imageUrl || undefined, name, price: Number(price) });
      return;
    }

    const requestBody = createCreateRequest();

    if (requestBody) {
      onSubmit(requestBody);
    }
  };

  return (
    <form className="grid gap-4 rounded-card border border-line bg-surface p-5" onSubmit={handleSubmit}>
      {formErrorMessage || productOptions.errorMessage ? (
        <ErrorState message={formErrorMessage ?? productOptions.errorMessage ?? MESSAGES.COMMON.UNKNOWN_ERROR} />
      ) : null}
      <Input label="상품명" name="name" onChange={(event) => setName(event.target.value)} required value={name} />
      <Textarea
        label="상품 설명"
        name="description"
        onChange={(event) => setDescription(event.target.value)}
        value={description}
      />
      <Input
        label="가격"
        min="1"
        name="price"
        onChange={(event) => setPrice(event.target.value)}
        required
        type="number"
        value={price}
      />
      {!product ? (
        <Select
          label="상품 유형"
          onChange={(event) => productOptions.setMode(event.target.value as ProductOptionMode)}
          value={productOptions.mode}
        >
          <option value="single">{MESSAGES.ADMIN_PRODUCT.OPTION_MODE_SINGLE}</option>
          <option value="option">{MESSAGES.ADMIN_PRODUCT.OPTION_MODE_OPTION}</option>
        </Select>
      ) : null}
      {!product && productOptions.mode === "single" ? (
        <Input
          label="초기 재고"
          min="0"
          name="stock"
          onChange={(event) => setStock(event.target.value)}
          required
          type="number"
          value={stock}
        />
      ) : null}
      {!product && productOptions.mode === "option" ? (
        <ProductOptionFields
          combinations={productOptions.combinations}
          groups={productOptions.groups}
          onAddGroup={productOptions.addGroup}
          onGenerateCombinations={productOptions.generateCombinations}
          onRemoveGroup={productOptions.removeGroup}
          onUpdateCombination={productOptions.updateCombination}
          onUpdateGroup={productOptions.updateGroup}
        />
      ) : null}
      <Input
        label="이미지 URL"
        name="imageUrl"
        onChange={(event) => setImageUrl(event.target.value)}
        type="url"
        value={imageUrl}
      />
      <Button disabled={isSubmitting} type="submit">
        {isSubmitting ? "저장 중" : "저장"}
      </Button>
    </form>
  );
};
