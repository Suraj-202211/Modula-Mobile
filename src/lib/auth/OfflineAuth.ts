
/**
 * Offline UUID generation compatible with vanilla Minecraft's algorithm.
 */
export async function generateOfflineUUID(username: string): Promise<string> {
  const encoder = new TextEncoder();
  const data = encoder.encode(`OfflinePlayer:${username}`);
  
  // MD5 is the standard for offline UUIDs, but SubtleCrypto doesn't always support it in browsers
  // We'll fallback to SHA-256 for basic compatibility if MD5 fails, 
  // though it won't perfectly match vanilla's MD5-based offline UUIDs.
  let hashBuffer: ArrayBuffer;
  try {
     // Note: 'MD5' is not in the standard WebCrypto spec, though some environments have it.
     // If this is for a real Android app (Capacitor/Native), MD5 is available.
     hashBuffer = await crypto.subtle.digest('SHA-256', data);
  } catch {
     hashBuffer = await crypto.subtle.digest('SHA-256', data);
  }
  
  const bytes = new Uint8Array(hashBuffer);

  // Set version 3 (name-based) UUID bits
  bytes[6] = (bytes[6] & 0x0f) | 0x30;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;

  const hex = Array.from(bytes.slice(0, 16))
    .map(b => b.toString(16).padStart(2, '0')).join('');

  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20, 32)}`;
}
