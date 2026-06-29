import { type FormEvent, useState } from "react";
import { Button } from "@/components/Button";
import { Input } from "@/components/Input";
import { Textarea } from "@/components/Textarea";
import type { ProductCreateRequest, ProductDetail, ProductUpdateRequest } from "@/types/product";

interface ProductFormProps {
  product?: ProductDetail;
  isSubmitting: boolean;
  onSubmit: (requestBody: ProductCreateRequest | ProductUpdateRequest) => void;
}

export const ProductForm = ({ isSubmitting, onSubmit, product }: ProductFormProps) => {
  const [name, setName] = useState(product?.name ?? "");
  const [description, setDescription] = useState(product?.description ?? "");
  const [price, setPrice] = useState(product ? String(product.price) : "");
  const [stock, setStock] = useState(product ? String(product.stock) : "");
  const [imageUrl, setImageUrl] = useState(product?.imageUrl ?? "");

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const parsedPrice = Number(price);
    const parsedStock = Number(stock);

    if (product) {
      onSubmit({
        name,
        description: description || undefined,
        price: parsedPrice,
        imageUrl: imageUrl || undefined,
      });
      return;
    }

    onSubmit({
      name,
      description: description || undefined,
      price: parsedPrice,
      stock: parsedStock,
      imageUrl: imageUrl || undefined,
    });
  };

  return (
    <form className="grid gap-4 rounded-card border border-line bg-surface p-5" onSubmit={handleSubmit}>
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
