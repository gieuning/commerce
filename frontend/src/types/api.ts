export interface ApiErrorResponse {
  errorCode: string;
  errorMessage: string;
}

export interface ApiResponse<TResponse> {
  data?: TResponse;
  error?: ApiErrorResponse;
}

export interface PageResult<TItem> {
  content: TItem[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
