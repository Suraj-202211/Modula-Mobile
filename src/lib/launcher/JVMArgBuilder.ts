
export interface LaunchConfig {
  javaPath: string;
  ramMb: number;
  versionId: string;
  username: string;
  uuid: string;
  accessToken: string;
  userType: string;
  gameDir: string;
  assetsDir: string;
  assetIndex: string;
  classpath: string;
  mainClass: string;
  nativesDir: string;
  performanceMode?: boolean; // New optional flag
}

/**
 * Builds the EXACT command that would launch Minecraft.
 * Based on vanilla Minecraft launcher logic.
 */
export function buildLaunchCommand(config: LaunchConfig): string {
  const {
    javaPath, ramMb, versionId, username,
    uuid, accessToken, userType,
    gameDir, assetsDir, assetIndex,
    classpath, mainClass, nativesDir,
    performanceMode
  } = config;

  const jvmArgs = [
    `-Xmx${ramMb}M`,
    `-Xms512M`,
    `-XX:+UseG1GC`,
    `-XX:+ParallelRefProcEnabled`,
    `-XX:MaxGCPauseMillis=150`,
    `-XX:+UnlockExperimentalVMOptions`,
    `-XX:+DisableExplicitGC`,
    `-XX:G1NewSizePercent=20`,
    `-XX:G1MaxNewSizePercent=50`,
    `-XX:G1HeapRegionSize=16M`,
    `-XX:G1ReservePercent=15`,
    `-XX:G1HeapWastePercent=5`,
    `-XX:G1MixedGCCountTarget=8`,
    `-XX:InitiatingHeapOccupancyPercent=10`,
    `-XX:G1MixedGCLiveThresholdPercent=85`,
    `-XX:G1RSetUpdatingPauseTimePercent=3`,
    `-XX:SurvivorRatio=16`,
    `-XX:+PerfDisableSharedMem`,
    `-XX:MaxTenuringThreshold=1`,
    // Performance Mode specific flags - INSANE OPTIMIZATION
    ...(performanceMode ? [
      '-XX:+UseStringDeduplication',
      '-XX:+OptimizeStringConcat',
      '-XX:+AlwaysPreTouch',
      '-XX:InitialCodeCacheSize=128M',
      '-XX:ReservedCodeCacheSize=512M',
      '-XX:+UseCodeCacheFlushing',
      '-XX:+UseTransparentHugePages',
      '-XX:+UseNUMA',
      '-XX:+UnlockExperimentalVMOptions',
      '-XX:G1NewSizePercent=40',
      '-XX:G1MaxNewSizePercent=50',
      '-XX:G1HeapRegionSize=16M',
      '-XX:G1ReservePercent=15',
      '-XX:G1HeapWastePercent=5',
      '-XX:G1MixedGCCountTarget=4',
      '-XX:InitiatingHeapOccupancyPercent=20',
      '-XX:G1MixedGCLiveThresholdPercent=90',
      '-XX:G1RSetUpdatingPauseTimePercent=5',
      '-XX:MaxGCPauseMillis=50', // Target 50ms pause for zero stutter
      '-Xss2M' // Extra thread stack for heavy mods
    ] : []),
    `-Djava.library.path=${nativesDir}`,
    `-Dminecraft.launcher.brand=ModulaMobile`,
    `-Dminecraft.launcher.version=1.5.0`,
    `-cp ${classpath}`
  ].join(' ');

  const gameArgs = [
    `--username ${username}`,
    `--version ${versionId}`,
    `--gameDir ${gameDir}`,
    `--assetsDir ${assetsDir}`,
    `--assetIndex ${assetIndex}`,
    `--uuid ${uuid}`,
    `--accessToken ${accessToken}`,
    `--userType ${userType}`,
    `--versionType release`
  ].join(' ');

  return `${javaPath} ${jvmArgs} ${mainClass} ${gameArgs}`;
}

export const DEFAULT_JVM_ARGS = '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=150';
