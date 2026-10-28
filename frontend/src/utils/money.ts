import {CURRENCY_LOCALE, CURRENCY_CODE, STRIP_AMOUNT_PATTERN, DECIMAL_SEPARATOR_INPUT, DECIMAL_SEPARATOR_PARSED, AMOUNT_PATTERN} from '@/constants/money';

const formatter = new Intl.NumberFormat(CURRENCY_LOCALE, { style: 'currency', currency: CURRENCY_CODE });

export function euros(cents: number): string {
  return formatter.format(cents / 100);
}

export function parseAmount(input: string): string | null {
  const clean = input.replace(STRIP_AMOUNT_PATTERN, '').replace(DECIMAL_SEPARATOR_INPUT, DECIMAL_SEPARATOR_PARSED);
  if (!AMOUNT_PATTERN.test(clean)) return null;
  const value = Number(clean);
  return value > 0 ? value.toFixed(2) : null;
}
