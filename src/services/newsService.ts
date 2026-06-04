import { AppConfig } from '../config/AppConfig';

export interface NewsItem {
  id: string | number;
  title: string;
  tag: string;
  description: string;
  image: string;
  link?: string;
}

export const fetchNews = async (): Promise<NewsItem[]> => {
  try {
    const response = await fetch(AppConfig.newsUrl);
    if (!response.ok) throw new Error('Failed to fetch news');
    const data = await response.json();
    
    // Map GitHub entries to our NewsItem interface
    return data.entries.map((entry: any) => ({
      id: entry.id,
      title: entry.title,
      tag: entry.tag,
      description: entry.text,
      image: entry.playPageImage?.url || entry.newsPageImage?.url || '',
      link: entry.readMoreLink
    }));
  } catch (error) {
    console.error('Error fetching news from GitHub:', error);
    return [];
  }
};
