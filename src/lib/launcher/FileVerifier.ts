
/**
 * Real SHA1 verification of downloaded files using Web Crypto API.
 */
export async function verifySHA1(
  data: ArrayBuffer,
  expectedHash: string
): Promise<boolean> {
  const hashBuffer = await crypto.subtle.digest('SHA-1', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex.toLowerCase() === expectedHash.toLowerCase();
}

export interface VerificationResult {
  verified: number;
  total: number;
  failed: string[];
}

/**
 * Simulates checking local storage (IndexedDB) for files and verifying them.
 * In a real environment, this would hit the actual filesystem.
 */
export async function verifyAllGameFiles(
  versionId: string,
  onProgress: (current: number, total: number, file: string) => void
): Promise<VerificationResult> {
  // Mocking the Mojang version JSON fetch for metadata
  // In reality, this would be cached in IndexedDB
  const files = [
    { path: 'client.jar', sha1: 'bb66f208-mock-hash-1' },
    { path: 'libraries/lwjgl-3.3.1.jar', sha1: '2a1f-mock-2' },
    { path: 'libraries/authlib-3.11.49.jar', sha1: '3c4d-mock-3' },
    { path: 'assets/indexes/1.20.json', sha1: '4d5e-mock-4' }
  ];

  let verified = 0;
  const failed: string[] = [];

  for (const file of files) {
    onProgress(verified, files.length, file.path);
    
    // Simulate verification delay
    await new Promise(r => setTimeout(r, 100));
    
    // For demo purposes, we'll assume files are present but some might "fail" 
    // if we wanted to show error states.
    verified++;
  }

  return { verified, total: files.length, failed };
}
