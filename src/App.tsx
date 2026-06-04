/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import { Home, Layers, Puzzle, Package, Settings, User, Bell, Zap, Play } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

import { 
  FluxBottomNav, 
  FluxTopBar, 
  FluxLoadingOverlay,
  FluxProgressBar
} from '@/src/components/glass/GlassComponents';
import { FluxParticles } from '@/src/components/glass/FluxParticles';
import { useLauncherStore } from './store/launcherStore';
import { useTranslation } from './lib/translations';

// Placeholder Screens
const HomeScreen = React.lazy(() => import('./screens/HomeScreen'));
const VersionsScreen = React.lazy(() => import('./screens/VersionsScreen'));
const ModsScreen = React.lazy(() => import('./screens/ModsScreen'));
const ModpacksScreen = React.lazy(() => import('./screens/ModpacksScreen'));
const SettingsScreen = React.lazy(() => import('./screens/SettingsScreen'));
const ProfileScreen = React.lazy(() => import('./screens/ProfileScreen'));
const LoginScreen = React.lazy(() => import('./screens/LoginScreen'));
const LaunchScreen = React.lazy(() => import('./screens/LaunchScreen'));
const InstallGuideScreen = React.lazy(() => import('./screens/InstallGuideScreen'));

const NavigationWrapper = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isScrolled, setIsScrolled] = useState(false);
  const { isLaunching, launchProgress, user, settings } = useLauncherStore();
  const t_fn = useTranslation(settings.language);

  const THEMES = {
    DEFAULT: {
      primary: 'bg-flux-gold/5',
      secondary: 'bg-flux-dim/3',
      accent: '#ffd700',
      gridOpacity: 'opacity-[0.05]',
      gridStyle: 'circle',
      bgType: 'particles'
    },
    ONYX: {
      primary: 'bg-slate-900/40',
      secondary: 'bg-slate-800/20',
      accent: '#334155',
      gridOpacity: 'opacity-[0.05]',
      gridStyle: 'mesh',
      bgType: 'none'
    },
    VOLCANIC: {
      primary: 'bg-red-600/15',
      secondary: 'bg-orange-800/10',
      accent: '#dc2626',
      gridOpacity: 'opacity-[0.1]',
      gridStyle: 'lines',
      bgType: 'particles'
    },
    NEON: {
      primary: 'bg-fuchsia-600/20',
      secondary: 'bg-indigo-600/15',
      accent: '#c026d3',
      gridOpacity: 'opacity-[0.15]',
      gridStyle: 'mesh',
      bgType: 'particles'
    },
    ARCTIC: {
      primary: 'bg-sky-400/15',
      secondary: 'bg-white/10',
      accent: '#38bdf8',
      gridOpacity: 'opacity-[0.08]',
      gridStyle: 'mesh',
      bgType: 'static'
    }
  };

  const themeConfig = THEMES[settings.theme] || THEMES.DEFAULT;

  useEffect(() => {
    document.body.setAttribute('data-performance-mode', settings.performanceMode ? 'true' : 'false');
  }, [settings.performanceMode]);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', settings.theme);
  }, [settings.theme]);

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navItems = [
    { id: '/', label: t_fn('home') },
    { id: '/versions', label: t_fn('versions') },
    { id: '/mods', label: t_fn('mods') },
    { id: '/modpacks', label: t_fn('modpacks') },
    { id: '/settings', label: t_fn('settings') },
    { id: '/profile', label: t_fn('profile') },
  ];

  const currentTab = navItems.find(item => item.id === location.pathname)?.id || '/';
  const isLaunchPage = location.pathname === '/launch';
  const activeIndex = navItems.findIndex(item => item.id === (location.pathname === '/' ? '/' : location.pathname));

  if (!user) {
    return (
      <div className="min-h-screen bg-bg-1 flex flex-col relative overflow-hidden">
        {/* Atmospheric Background */}
        <div className="fixed inset-0 pointer-events-none -z-10 bg-bg-1">
          <div className={`absolute top-[-10%] left-[-10%] w-[50%] h-[50%] ${themeConfig.primary} blur-[120px] rounded-full animate-pulse-slow will-change-opacity`} />
          <div className={`absolute bottom-[-10%] right-[-10%] w-[60%] h-[60%] ${themeConfig.secondary} blur-[150px] rounded-full animate-pulse-slow delay-700 will-change-opacity`} />
        </div>
        <FluxParticles />
        <React.Suspense fallback={<div className="flex items-center justify-center h-screen bg-bg-1"><Zap className="animate-pulse text-flux-gold" /></div>}>
          <LoginScreen />
        </React.Suspense>
      </div>
    );
  }

  return (
    <div 
      className="min-h-screen bg-bg-1 flex flex-col pb-24 relative overflow-hidden transition-all duration-300"
      style={{ fontSize: `${settings.uiScaling / 100}rem` }}
    >
      {/* Atmospheric Background */}
      <div className="fixed inset-0 pointer-events-none -z-10 bg-bg-1 overflow-hidden">
        {/* Technical Grid Pattern */}
        {themeConfig.gridStyle === 'circle' && (
          <div 
            className={`absolute inset-0 ${themeConfig.gridOpacity}`} 
            style={{ 
              backgroundImage: `radial-gradient(circle, ${themeConfig.accent} 1px, transparent 1px)`, 
              backgroundSize: '40px 40px' 
            }} 
          />
        )}
        {themeConfig.gridStyle === 'lines' && (
          <div 
            className={`absolute inset-0 ${themeConfig.gridOpacity}`} 
            style={{ 
              backgroundImage: `linear-gradient(${themeConfig.accent} 0.5px, transparent 0.5px)`, 
              backgroundSize: '100% 4px' 
            }} 
          />
        )}
        {themeConfig.gridStyle === 'mesh' && (
          <div 
            className={`absolute inset-0 ${themeConfig.gridOpacity}`} 
            style={{ 
              backgroundImage: `linear-gradient(${themeConfig.accent} 1px, transparent 1px), linear-gradient(90deg, ${themeConfig.accent} 1px, transparent 1px)`, 
              backgroundSize: '80px 80px' 
            }} 
          />
        )}
        
        {!settings.lowRamMode && (
          <>
            <div className={`absolute top-[-20%] right-[-20%] w-[70%] h-[70%] ${themeConfig.primary} blur-[140px] rounded-full animate-pulse-slow will-change-opacity`} />
            <div className={`absolute bottom-[-20%] left-[-20%] w-[60%] h-[60%] ${themeConfig.secondary} blur-[160px] rounded-full animate-pulse-slow delay-1000 will-change-opacity`} />
          </>
        )}
      </div>
      
      {themeConfig.bgType === 'particles' && <FluxParticles />}
      
      {!isLaunchPage && (
        <FluxTopBar 
          title={location.pathname === '/' ? 'MODULA' : location.pathname.substring(1).toUpperCase()}
          isScrolled={isScrolled}
          actions={
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-state-success shadow-[0_0_8px_rgba(34,197,94,0.8)]" />
              <button className="p-2 text-text-muted hover:text-white transition-colors">
                <Bell size={20} />
              </button>
            </div>
          }
        />
      )}

      <main className="flex-grow px-4 max-w-lg mx-auto w-full relative z-10">
        <React.Suspense fallback={<div className="flex items-center justify-center h-64"><Zap className="animate-pulse text-flux-gold" /></div>}>
          <Routes location={location}>
            <Route path="/" element={<HomeScreen />} />
            <Route path="/versions" element={<VersionsScreen />} />
            <Route path="/mods" element={<ModsScreen />} />
            <Route path="/modpacks" element={<ModpacksScreen />} />
            <Route path="/settings" element={<SettingsScreen />} />
            <Route path="/profile" element={<ProfileScreen />} />
            <Route path="/launch" element={<LaunchScreen />} />
            <Route path="/install-guide" element={<InstallGuideScreen />} />
          </Routes>
        </React.Suspense>
      </main>
      
      {!isLaunchPage && (
        <FluxBottomNav 
          items={navItems} 
          selectedId={currentTab} 
          onSelect={(id) => navigate(id)} 
        />
      )}
    </div>
  );
};

export default function App() {
  return (
    <Router>
      <NavigationWrapper />
    </Router>
  );
}

