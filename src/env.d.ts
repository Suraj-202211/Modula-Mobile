/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_GITHUB_USERNAME: string;
  readonly VITE_MODULA_NEWS_URL: string;
  readonly VITE_MODULA_DISCORD: string;
  readonly VITE_MODULA_DOWNLOAD: string;
  // add other variables here...
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
