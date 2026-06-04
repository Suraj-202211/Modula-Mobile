import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { ModResult } from '../services/modService';

export interface MinecraftAccount {
  username: string;
  uuid: string;
  email?: string;
  type: 'MICROSOFT' | 'OFFLINE';
  isPremium: boolean;
  joinedDate: number;
}

export interface ActivityItem {
  id: string;
  type: 'PLAYED' | 'DOWNLOADED' | 'MOD_INSTALLED';
  versionId: string;
  loader?: string;
  timestamp: number;
}

export enum LaunchState {
  IDLE = 'IDLE',
  PRE_LAUNCH = 'PRE_LAUNCH',
  LAUNCHING = 'LAUNCHING',
  GAME_RUNNING = 'GAME_RUNNING',
  ERROR = 'ERROR'
}

interface Settings {
  ramAllocation: number;
  fpsUnlock: boolean;
  jvmArgs: string;
  particleIntensity: number;
  motionBlur: number;
  bloomEffect: boolean; // New
  dynamicShadows: boolean; // New
  transparencyLevel: number; // New (0-100)
  touchHaptics: boolean; // New
  theme: 'DEFAULT' | 'ONYX' | 'VOLCANIC' | 'NEON' | 'ARCTIC'; // New
  autoUpdate: boolean; // New
  showFPS: boolean; // New
  uiScaling: number; // New (80-120)
  language: string; // New
  debugLogs: boolean; // New
  enableSnapshots: boolean; // New
  lowRamMode: boolean; // New
  batterySaver: boolean; // New
  gpuAcceleration: boolean; // New
  fluxVoiceEnabled: boolean; // New
  fluxVoiceProximity: boolean; // New
  fluxVoiceNoiseSuppression: boolean; // New
  fluxVoiceHighBitrate: boolean; // New
  richPresence: boolean;
  performanceMode: boolean; // New setting
  lastPlayedVersionId: string;
  lastPlayedLoader: string;
}

interface LauncherState {
  user: MinecraftAccount | null;
  selectedVersion: string;
  isLaunching: boolean;
  launchState: LaunchState;
  launchProgress: number;
  launchLogs: string[];
  launchError: string | null;
  activities: ActivityItem[];
  settings: Settings;
  installedMods: ModResult[];
  installedModpacks: ModResult[]; 
  bridgeConnected: boolean;
  customSkinUrl: string | null;
  skinAnimation: 'standing' | 'walking' | 'running';
  
  setUser: (user: MinecraftAccount | null) => void;
  setSelectedVersion: (version: string) => void;
  setLaunching: (launching: boolean) => void;
  setLaunchState: (state: LaunchState) => void;
  setLaunchProgress: (progress: number) => void;
  setLaunchError: (error: string | null) => void;
  setBridgeConnected: (connected: boolean) => void;
  setCustomSkinUrl: (url: string | null) => void;
  setSkinAnimation: (anim: 'standing' | 'walking' | 'running') => void;
  addLaunchLog: (log: string) => void;
  clearLaunchLogs: () => void;
  addActivity: (activity: Omit<ActivityItem, 'id' | 'timestamp'>) => void;
  updateSettings: (settings: Partial<Settings>) => void;
  installMod: (mod: ModResult) => void;
  installModpack: (modpack: ModResult) => void; // New action
  loginMicrosoft: (username: string, email: string, uuid: string) => void;
  loginOffline: (username: string) => void;
  logout: () => void;
}

export const useLauncherStore = create<LauncherState>()(
  persist(
    (set) => ({
      user: null, 
      selectedVersion: '1.20.1',
      isLaunching: false,
      launchState: LaunchState.IDLE,
      launchProgress: 0,
      launchLogs: [],
      launchError: null,
      activities: [],
      installedMods: [],
      installedModpacks: [],
      bridgeConnected: false,
      customSkinUrl: null,
      skinAnimation: 'standing',
      settings: {
        ramAllocation: 4096,
        fpsUnlock: true,
        jvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=150',
        particleIntensity: 60,
        motionBlur: 80,
        bloomEffect: true,
        dynamicShadows: true,
        transparencyLevel: 10,
        touchHaptics: true,
        theme: 'DEFAULT',
        autoUpdate: true,
        showFPS: false,
        uiScaling: 100,
        language: 'English (US)',
        debugLogs: false,
        enableSnapshots: false,
        lowRamMode: false,
        batterySaver: false,
        gpuAcceleration: true,
        fluxVoiceEnabled: true,
        fluxVoiceProximity: true,
        fluxVoiceNoiseSuppression: true,
        fluxVoiceHighBitrate: true,
        richPresence: true,
        performanceMode: false,
        lastPlayedVersionId: '1.20.1',
        lastPlayedLoader: 'vanilla'
      },

      setUser: (user) => set({ user }),
      setSelectedVersion: (selectedVersion) => set({ selectedVersion }),
      setLaunching: (isLaunching) => set({ isLaunching }),
      setLaunchState: (launchState) => set({ launchState }),
      setLaunchProgress: (launchProgress) => set({ launchProgress }),
      setLaunchError: (launchError) => set({ launchError }),
      setBridgeConnected: (bridgeConnected) => set({ bridgeConnected }),
      setCustomSkinUrl: (customSkinUrl) => set({ customSkinUrl }),
      setSkinAnimation: (skinAnimation) => set({ skinAnimation }),
      
      addLaunchLog: (log) => set((state) => ({
        launchLogs: [...state.launchLogs, log].slice(-50)
      })),

      clearLaunchLogs: () => set({ launchLogs: [] }),

      addActivity: (activity) => set((state) => ({
        activities: [
          { 
            ...activity, 
            id: Math.random().toString(36).substr(2, 9), 
            timestamp: Date.now() 
          }, 
          ...state.activities
        ].slice(0, 20)
      })),

      updateSettings: (newSettings) => set((state) => ({
        settings: { ...state.settings, ...newSettings }
      })),

      installMod: (mod) => set((state) => ({
        installedMods: [...state.installedMods, mod],
        activities: [
          {
            id: Math.random().toString(36).substr(2, 9),
            type: 'MOD_INSTALLED',
            versionId: state.selectedVersion,
            loader: 'Fabric',
            timestamp: Date.now()
          },
          ...state.activities
        ]
      })),

      installModpack: (modpack) => set((state) => ({
        installedModpacks: [...state.installedModpacks, modpack],
        activities: [
          {
            id: Math.random().toString(36).substr(2, 9),
            type: 'DOWNLOADED',
            versionId: modpack.title,
            loader: 'Modpack',
            timestamp: Date.now()
          },
          ...state.activities
        ]
      })),
      
      loginMicrosoft: (username, email, uuid) => set({ 
        user: { 
          username, 
          email, 
          uuid,
          type: 'MICROSOFT',
          isPremium: true,
          joinedDate: Date.now()
        } 
      }),

      loginOffline: async (username) => {
        const { generateOfflineUUID } = await import('../lib/auth/OfflineAuth');
        const uuid = await generateOfflineUUID(username);
        set({ 
          user: { 
            username, 
            uuid, 
            type: 'OFFLINE',
            isPremium: false,
            joinedDate: Date.now()
          } 
        });
      },
      
      logout: () => set({ user: null, activities: [] }),
    }),
    {
      name: 'modula-launcher-storage',
    }
  )
);
