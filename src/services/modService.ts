export interface ModResult {
  project_id: string;
  project_type: string;
  slug: string;
  author: string;
  title: string;
  description: string;
  categories: string[];
  display_categories: string[];
  versions: string[];
  downloads: number;
  follows: number;
  icon_url: string;
  date_created: string;
  date_modified: string;
  latest_version: string;
  license: string;
  client_side: string;
  server_side: string;
  gallery: string[];
  featured_gallery: string | null;
}

export interface ModSearchResponse {
  hits: ModResult[];
  offset: number;
  limit: number;
  total_hits: number;
}

export const searchMods = async (query: string = '', limit: number = 10): Promise<ModSearchResponse> => {
  const response = await fetch(`/api/mods/search?query=${query}&limit=${limit}&type=mod`);
  if (!response.ok) throw new Error('Failed to search mods');
  return response.json();
};

export const searchModpacks = async (query: string = '', limit: number = 10): Promise<ModSearchResponse> => {
  const response = await fetch(`/api/mods/search?query=${query}&limit=${limit}&type=modpack`);
  if (!response.ok) throw new Error('Failed to search modpacks');
  return response.json();
};
