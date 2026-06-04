import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Zap, Play, Cpu, ShieldCheck, ChevronRight, Layers, Terminal as TerminalIcon, Mic } from 'lucide-react';
import { 
  FluxHeroCard, 
  FluxButton, 
  FluxCard, 
  GlassVariant, 
  FluxBadge,
  FluxChip,
  FluxSectionHeader,
  FluxListTile,
  FluxIconButton,
  FluxDivider
} from '../components/glass/GlassComponents';
import { fetchNews, NewsItem } from '../services/newsService';
import { useLauncherStore } from '../store/launcherStore';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from '../lib/translations';

const HomeScreen: React.FC = () => {
  const navigate = useNavigate();
  const [news, setNews] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const { 
    user, 
    selectedVersion, 
    settings, 
    activities, 
  } = useLauncherStore();

  const t_fn = useTranslation(settings.language);

  useEffect(() => {
    fetchNews().then(data => {
      setNews(data);
      setLoading(false);
    }).catch(err => {
      console.error(err);
      setLoading(false);
    });
  }, []);

  const handleLaunch = () => {
    // Basic pre-checks before navigating to LaunchScreen
    if (!user) {
      alert("Please sign in to play Minecraft.");
      navigate('/profile');
      return;
    }
    
    // In a real app, we'd check if version is downloaded here
    // For now, we'll assume it's ready and navigate to LaunchScreen
    navigate('/launch');
  };

  const getRelativeTime = (timestamp: number) => {
    const diff = Date.now() - timestamp;
    if (diff < 60000) return 'just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    return `${Math.floor(diff / 86400000)}d ago`;
  };

  const engineStatus = 
    settings.fpsUnlock && settings.ramAllocation >= 4096 ? { label: 'ACTIVE', color: 'bg-state-success' } :
    settings.fpsUnlock || settings.ramAllocation >= 4096 ? { label: 'PARTIAL', color: 'bg-flux-gold' } :
    { label: 'INACTIVE', color: 'bg-state-error' };

  if (!user) return null;

  const isMicrosoft = user.type === 'MICROSOFT';
  const avatarUrl = isMicrosoft 
    ? `https://mc-heads.net/avatar/${user.uuid}/64` 
    : `https://mc-heads.net/avatar/${user.username}/64`;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{ duration: 0.4 }}
      className="space-y-6 pt-4 pb-12"
    >
      <FluxHeroCard 
        backgroundImage="https://images.unsplash.com/photo-1627398242454-45a1465c2479?q=80&w=1000&auto=format&fit=crop"
        className="h-64 shadow-2xl"
      >
        <div className="flex justify-between items-start w-full">
          <FluxChip label={`v${selectedVersion}`} selected className="font-bold backdrop-blur-xl" />
          <div className={`w-12 h-12 rounded-full border-2 p-1 shadow-[0_0_20px_rgba(255,215,0,0.3)] ${isMicrosoft ? 'border-flux-gold' : 'border-white/20'}`}>
            <img 
              src={avatarUrl} 
              alt="Avatar" 
              className="w-full h-full rounded-full bg-bg-2"
              referrerPolicy="no-referrer"
            />
          </div>
        </div>

        <div className="flex flex-col items-center gap-4 mt-auto">
          <div className="text-center">
            <h2 className="text-2xl font-display font-bold text-white uppercase tracking-tight">{user.username}</h2>
            <p className="text-xs text-text-secondary uppercase tracking-widest mt-1">{t_fn('readyToLaunch')}</p>
          </div>
          
          <FluxButton 
            label={t_fn('launchGame')} 
            icon={<Play size={18} fill="currentColor" />}
            className="w-full max-w-[240px] py-4 text-lg"
            onClick={handleLaunch}
          />
          
          <p className="text-[10px] text-text-muted uppercase tracking-[0.2em]">
            {user.type} • {(settings.ramAllocation / 1024).toFixed(0)}GB RAM
          </p>
        </div>
      </FluxHeroCard>

      <div className="grid grid-cols-3 gap-3">
        <FluxCard variant={GlassVariant.DARK} className="flex flex-col items-center justify-center p-3 text-center">
          <Cpu size={16} className="text-flux-gold mb-2" />
          <span className="text-[10px] font-bold text-white uppercase tracking-tighter">{(settings.ramAllocation / 1024).toFixed(0)}GB</span>
          <span className="text-[8px] text-text-muted uppercase">{t_fn('allocated')}</span>
        </FluxCard>
        <FluxCard variant={GlassVariant.DARK} className="flex flex-col items-center justify-center p-3 text-center">
          <Zap size={16} className={`${settings.fpsUnlock ? 'text-flux-gold' : 'text-text-muted'} mb-2`} fill={settings.fpsUnlock ? 'currentColor' : 'none'} />
          <span className="text-[10px] font-bold text-white uppercase tracking-tighter">{settings.fpsUnlock ? 'Unlocked' : 'Capped'}</span>
          <span className="text-[8px] text-text-muted uppercase">Engine</span>
        </FluxCard>
        <FluxCard variant={GlassVariant.DARK} className="flex flex-col items-center justify-center p-3 text-center">
          <ShieldCheck size={16} className="text-state-success mb-2" />
          <span className="text-[10px] font-bold text-white uppercase tracking-tighter text-state-success">{t_fn('active')}</span>
          <span className="text-[8px] text-text-muted uppercase">{t_fn('security')}</span>
        </FluxCard>
        {settings.fluxVoiceEnabled && (
          <FluxCard variant={GlassVariant.DARK} className="flex flex-col items-center justify-center p-3 text-center">
            <Mic size={16} className="text-flux-gold mb-2" />
            <span className="text-[10px] font-bold text-white uppercase tracking-tighter text-flux-gold">ON</span>
            <span className="text-[8px] text-text-muted uppercase">Voice</span>
          </FluxCard>
        )}
      </div>

      <FluxCard variant={GlassVariant.GOLD} className="flex items-center gap-4 p-4">
        <div className="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center text-white">
          <Zap size={24} fill="currentColor" />
        </div>
        <div className="flex-grow">
          <h3 className="text-sm font-display font-bold text-white uppercase">Golden Flux Engine</h3>
          <p className="text-[10px] text-white/70 uppercase tracking-widest mt-0.5">144Hz UI • Zero Stutter • Hardware Security</p>
        </div>
        <FluxBadge label={engineStatus.label} color={`${engineStatus.color} text-white border-white/20`} />
      </FluxCard>

      <FluxSectionHeader 
        title={t_fn('modernModpacks')} 
        action={<button onClick={() => navigate('/modpacks')} className="text-[10px] font-bold text-flux-gold flex items-center gap-1 uppercase tracking-widest">{t_fn('exploreAll')} <ChevronRight size={12} /></button>} 
      />

      <div className="flex gap-4 overflow-x-auto no-scrollbar pb-2">
        <div 
          onClick={() => navigate('/modpacks')}
          className="min-w-[200px] h-32 relative rounded-2xl overflow-hidden cursor-pointer group"
        >
          <img src="https://images.unsplash.com/photo-1549467354-9493f3d2779a?q=80&w=400&auto=format&fit=crop" className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-200 will-change-transform" alt="Better Minecraft" referrerPolicy="no-referrer" />
          <div className="absolute inset-0 bg-gradient-to-t from-bg-1 to-transparent" />
          <div className="absolute bottom-3 left-3">
             <h4 className="text-xs font-display font-black text-white uppercase tracking-tight">Better Minecraft</h4>
              <p className="text-[8px] text-white/60 uppercase tracking-widest">Fabric 1.20.1</p>
          </div>
        </div>
        <div 
          onClick={() => navigate('/modpacks')}
          className="min-w-[200px] h-32 relative rounded-2xl overflow-hidden cursor-pointer group"
        >
          <img src="https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400&auto=format&fit=crop" className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-200 will-change-transform" alt="RLCraft" referrerPolicy="no-referrer" />
          <div className="absolute inset-0 bg-gradient-to-t from-bg-1 to-transparent" />
          <div className="absolute bottom-3 left-3">
             <h4 className="text-xs font-display font-black text-white uppercase tracking-tight">RLCraft Optimized</h4>
              <p className="text-[8px] text-white/60 uppercase tracking-widest">Forge 1.12.2</p>
          </div>
        </div>
      </div>

      <FluxSectionHeader 
        title={t_fn('recentActivity')} 
        action={<button className="text-[10px] font-bold text-flux-gold flex items-center gap-1 uppercase tracking-widest">{t_fn('exploreAll')} <ChevronRight size={12} /></button>} 
      />

      <div className="space-y-3">
        {activities.length === 0 ? (
          <div className="py-8 text-center glass-dark rounded-xl border border-dashed border-white/10">
            <p className="text-xs text-text-muted uppercase tracking-widest">No recent activity. Download a version to get started.</p>
          </div>
        ) : (
          activities.slice(0, 3).map((act) => (
            <FluxListTile 
              key={act.id}
              title={`${act.loader || 'Minecraft'} ${act.versionId}`} 
              subtitle={`${act.type === 'PLAYED' ? 'Played' : 'Downloaded'} ${getRelativeTime(act.timestamp)}`} 
              leading={<div className="w-10 h-10 bg-bg-4 rounded-md flex items-center justify-center"><Layers className="text-flux-gold" size={20} /></div>}
              trailing={<FluxIconButton icon={<Play size={14} fill="currentColor" />} size="SMALL" />}
            />
          ))
        )}
      </div>

      <FluxSectionHeader title={t_fn('communityNews')} />
      <div className="space-y-4">
        {loading ? (
          <div className="animate-pulse space-y-4">
             <div className="h-48 glass-dark rounded-xl" />
             <div className="h-48 glass-dark rounded-xl" />
          </div>
        ) : (
          news.map((item, i) => (
            <motion.div
              key={item.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
              onClick={() => item.link && window.open(item.link, '_blank')}
              className={item.link ? "cursor-pointer" : ""}
            >
              <FluxCard variant={GlassVariant.DARK} className="overflow-hidden p-0">
                <div className="h-32 bg-cover bg-center" style={{ backgroundImage: `url(${item.image})` }}>
                  <div className="inset-0 bg-gradient-to-t from-bg-3 to-transparent h-full w-full" />
                </div>
                <div className="p-4">
                  <FluxBadge label={item.tag} className="mb-2" />
                  <h4 className="text-md font-bold text-white mb-1">{item.title}</h4>
                  <p className="text-xs text-text-secondary line-clamp-2">{item.description}</p>
                </div>
              </FluxCard>
            </motion.div>
          ))
        )}
      </div>

    </motion.div>
  );
};

export default HomeScreen;
