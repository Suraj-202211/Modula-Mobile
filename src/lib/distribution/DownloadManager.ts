import { AppConfig } from '../../config/AppConfig';

export interface ReleaseInfo {
  tagName: string;
  size: number;
  publishedAt: string;
}

export const DownloadManager = {
  async fetchLatestRelease(): Promise<ReleaseInfo | null> {
    const cached = sessionStorage.getItem('modula_latest_release');
    if (cached) {
      const data = JSON.parse(cached);
      if (Date.now() - data.timestamp < 1800000) { // 30 mins
        return data.info;
      }
    }

    try {
      const response = await fetch(AppConfig.githubApiUrl);
      if (!response.ok) throw new Error('Release not found');
      const data = await response.json();
      
      const info = {
        tagName: data.tag_name,
        size: data.assets[0]?.size || 0,
        publishedAt: data.published_at
      };

      sessionStorage.setItem('modula_latest_release', JSON.stringify({
        timestamp: Date.now(),
        info
      }));

      return info;
    } catch (error) {
      console.error('Failed to fetch latest release:', error);
      return {
        tagName: AppConfig.appVersion,
        size: 32000000, // 32MB fallback
        publishedAt: new Date().toISOString()
      };
    }
  },

  downloadAPK() {
    window.open(AppConfig.apkDownloadUrl, '_blank');
  },

  isAndroid() {
    return /Android/i.test(navigator.userAgent);
  }
};
