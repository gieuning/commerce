import { MESSAGES } from "@/constants/messages";
import { PAYMENT_METHOD_LABELS, TOSS_PAYMENTS_SCRIPT_URL } from "@/constants/payment";
import type { PaymentMethod } from "@/types/payment";

interface TossPaymentRequest {
  amount: number;
  clientKey: string;
  customerName?: string;
  failUrl: string;
  method: PaymentMethod;
  merchantOrderId: string;
  orderName: string;
  successUrl: string;
}

interface TossPaymentsClient {
  requestPayment: (
    method: string,
    options: {
      amount: number;
      customerName?: string;
      failUrl: string;
      orderId: string;
      orderName: string;
      successUrl: string;
    },
  ) => Promise<void>;
}

declare global {
  interface Window {
    TossPayments?: (clientKey: string) => TossPaymentsClient;
  }
}

let tossScriptLoadPromise: Promise<void> | null = null;

type TossScriptLoadState = "loading" | "loaded" | "failed";

const TOSS_SCRIPT_LOAD_STATE_ATTRIBUTE = "data-load-state";

const resetTossScriptLoadPromise = () => {
  tossScriptLoadPromise = null;
};

const setScriptLoadState = (script: HTMLScriptElement, state: TossScriptLoadState) => {
  script.dataset.loadState = state;
};

const getScriptLoadState = (script: HTMLScriptElement): TossScriptLoadState | null => {
  const loadState = script.dataset.loadState;
  return loadState === "loading" || loadState === "loaded" || loadState === "failed"
    ? loadState
    : null;
};

const createScriptLoadFailure = (script?: HTMLScriptElement): Error => {
  if (script) {
    setScriptLoadState(script, "failed");
  }

  resetTossScriptLoadPromise();
  return new Error(MESSAGES.PAYMENT.SCRIPT_LOAD_FAILED);
};

const listenScriptLoad = (script: HTMLScriptElement): Promise<void> =>
  new Promise((resolve, reject) => {
    script.addEventListener(
      "load",
      () => {
        setScriptLoadState(script, "loaded");
        resolve();
      },
      { once: true },
    );
    script.addEventListener("error", () => reject(createScriptLoadFailure(script)), {
      once: true,
    });
  });

const loadTossPaymentScript = (): Promise<void> => {
  if (window.TossPayments) {
    return Promise.resolve();
  }

  if (tossScriptLoadPromise) {
    return tossScriptLoadPromise;
  }

  tossScriptLoadPromise = new Promise((resolve, reject) => {
    const existingScript = document.querySelector<HTMLScriptElement>(
      `script[src="${TOSS_PAYMENTS_SCRIPT_URL}"]`,
    );

    if (existingScript) {
      const loadState = getScriptLoadState(existingScript);

      if (loadState === null) {
        existingScript.remove();
        resetTossScriptLoadPromise();
        loadTossPaymentScript().then(resolve).catch(reject);
        return;
      }

      if (loadState === "loaded") {
        reject(createScriptLoadFailure(existingScript));
        return;
      }

      if (loadState === "failed") {
        existingScript.remove();
        resetTossScriptLoadPromise();
        loadTossPaymentScript().then(resolve).catch(reject);
        return;
      }

      listenScriptLoad(existingScript).then(resolve).catch(reject);
      return;
    }

    const tossScript = document.createElement("script");
    tossScript.src = TOSS_PAYMENTS_SCRIPT_URL;
    tossScript.async = true;
    tossScript.setAttribute(TOSS_SCRIPT_LOAD_STATE_ATTRIBUTE, "loading");
    listenScriptLoad(tossScript).then(resolve).catch(reject);
    document.head.appendChild(tossScript);
  });

  return tossScriptLoadPromise;
};

export const tossPaymentService = {
  requestPayment: async ({
    amount,
    clientKey,
    customerName,
    failUrl,
    method,
    merchantOrderId,
    orderName,
    successUrl,
  }: TossPaymentRequest): Promise<void> => {
    await loadTossPaymentScript();

    if (!window.TossPayments) {
      throw new Error(MESSAGES.PAYMENT.SCRIPT_LOAD_FAILED);
    }

    const tossPayments = window.TossPayments(clientKey);
    await tossPayments.requestPayment(PAYMENT_METHOD_LABELS[method], {
      amount,
      customerName,
      failUrl,
      orderId: merchantOrderId,
      orderName,
      successUrl,
    });
  },
};
