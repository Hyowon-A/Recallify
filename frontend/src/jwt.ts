function jwtExp(token: string): number | null {
  try {
    const part = token.split(".")[1];
    if (!part) return null;
    // base64url → base64 + padding
    const base64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    const json = atob(padded);
    const payload: unknown = JSON.parse(json);
    if (!payload || typeof payload !== "object") return null;
    const claims = payload as Record<string, unknown>;
    return typeof claims.exp === "number" ? claims.exp : null;
  } catch {
    return null;
  }
}

// consider tokens expiring within 60s as "expired"
export function isTokenExpired(token: string, skewSeconds = 60): boolean {
  const exp = jwtExp(token);
  if (!exp) return true;
  const now = Math.floor(Date.now() / 1000);
  return exp - now <= skewSeconds;
}

function jwtPayload(token: string): Record<string, unknown> | null {
  try {
    const part = token.split(".")[1];
    if (!part) return null;
    const b64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const pad = b64 + "=".repeat((4 - (b64.length % 4)) % 4);
    const payload: unknown = JSON.parse(atob(pad));
    return payload && typeof payload === "object"
      ? payload as Record<string, unknown>
      : null;
  } catch {
    return null;
  }
}

export function getUserFromToken(token: string): { email: string; name: string } | null {
  const p = jwtPayload(token);
  if (!p) return null;
  const email = typeof p.sub === "string" ? p.sub : null;
  const name  = localStorage.getItem("name");
  if (!email) return null;
  if (!name) return null;
  return { email, name };
}
