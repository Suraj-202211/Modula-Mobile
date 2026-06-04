export const AppConfig = {
  githubUsername: import.meta.env.VITE_GITHUB_USERNAME || 'NOVE300IQ',
  githubRepo: import.meta.env.VITE_GITHUB_REPO || 'modula-mobile',
  azureClientId: import.meta.env.VITE_AZURE_CLIENT_ID || '',
  website: import.meta.env.VITE_APP_WEBSITE || 'https://www.modulamc.in',
  discordInvite: import.meta.env.VITE_DISCORD_INVITE || 'https://discord.gg/ZKaDavTxnJ',
  appVersion: import.meta.env.VITE_APP_VERSION || '1.0.0-GOLDEN',

  // Computed URLs
  get apkDownloadUrl() {
    return `https://github.com/${this.githubUsername}/${this.githubRepo}/releases/latest/download/ModulaMobile.apk`;
  },
  get releasesUrl() {
    return `https://github.com/${this.githubUsername}/${this.githubRepo}/releases`;
  },
  get githubApiUrl() {
    return `https://api.github.com/repos/${this.githubUsername}/${this.githubRepo}/releases/latest`;
  },
  get newsUrl() {
    return `https://raw.githubusercontent.com/${this.githubUsername}/modula-news/main/news.json`;
  },
  get buildDate() {
    return new Date().toISOString().slice(0, 10);
  }
};
