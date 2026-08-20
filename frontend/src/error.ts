export function errorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message ? error.message : fallback;
}

export function isAbortError(error: unknown) {
  return error instanceof Error && error.name === "AbortError";
}
