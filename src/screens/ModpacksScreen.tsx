import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Search, 
  Grid, 
  Download, 
  Check, 
  Loader2, 
  Package, 
  Layers, 
  Zap, 
  Info,
  Clock,
  ExternalLink,
  ChevronRight
} from 'lucide-react';
import { 
  FluxCard, 
  GlassVariant, 
  FluxChip, 
  FluxIconButton,
  FluxBadge,
  FluxButton,
  FluxDivider
} from '../components/glass/GlassComponents';
import { searchModpacks, ModResult } from '../services/modService';
import { useLauncherStore } from '../store/launcherStore';

const ModpackCard = React.memo(({ 
  modpack, 
  isInstalled, 
  onInstall, 
  isInstalling 
}: { 
  modpack: ModResult, 
  isInstalled: boolean, 
  onInstall: (m: ModResult) => void,
  isInstalling: boolean
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="group"
    >
      <FluxCard variant={GlassVariant.DARK} className="relative overflow-hidden p-0 h-full border-white/5 hover:border-flux-gold/30 transition-all duration-300">
        <div className="aspect-video w-full overflow-hidden relative">
          {modpack.icon_url ? (
            <img src={modpack.icon_url} alt={modpack.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
          ) : (
            <div className="w-full h-full bg-bg-3 flex items-center justify-center">
              <Package size={40} className="text-white/10" />
            </div>
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-bg-1/90 via-bg-1/20 to-transparent" />
          
          <div className="absolute top-3 right-3 flex gap-2">
            <FluxBadge label={modpack.categories[0] || 'GENERAL'} className="bg-bg-1/60 backdrop-blur-md border-white/10 text-[8px]" />
          </div>
        </div>

        <div className="p-4 space-y-3">
          <div>
            <h4 className="font-display font-black text-white text-base leading-tight line-clamp-1 uppercase tracking-tight group-hover:text-flux-gold transition-colors">
              {modpack.title}
            </h4>
            <p className="text-[10px] text-text-muted font-bold uppercase tracking-widest mt-1">
              By {modpack.author}
            </p>
          </div>

          <p className="text-[11px] text-white/50 line-clamp-2 h-8">
            {modpack.description}
          </p>

          <div className="flex items-center justify-between pt-2 border-t border-white/5">
            <div className="flex gap-4">
              <div className="flex flex-col">
                <span className="text-[8px] text-text-muted font-black uppercase tracking-widest">Downloads</span>
                <span className="text-xs text-white font-mono">{(modpack.downloads / 1000).toFixed(0)}K</span>
              </div>
              <div className="flex flex-col">
                <span className="text-[8px] text-text-muted font-black uppercase tracking-widest">Followers</span>
                <span className="text-xs text-white font-mono">{modpack.follows}</span>
              </div>
            </div>

            {isInstalled ? (
              <div className="flex items-center gap-2 text-state-success text-[10px] font-black uppercase tracking-widest">
                 <Check size={14} /> INSTALLED
              </div>
            ) : (
              <FluxButton 
                label={isInstalling ? "INSTALLING..." : "INSTALL"} 
                size="SMALL" 
                variant={isInstalling ? GlassVariant.DARK : GlassVariant.GOLD}
                className="px-6 h-8 text-[9px]"
                onClick={() => onInstall(modpack)}
                icon={isInstalling ? <Loader2 size={12} className="animate-spin" /> : <Download size={12} />}
              />
            )}
          </div>
        </div>
      </FluxCard>
    </motion.div>
  );
});

const ModpacksScreen: React.FC = () => {
  const navigate = useNavigate();
  const [tab, setTab] = useState('Browse');
  const [query, setQuery] = useState('');
  const [modpacks, setModpacks] = useState<ModResult[]>([]);
  const [loading, setLoading] = useState(false);
  const { installedModpacks, installModpack } = useLauncherStore();
  const [installingId, setInstallingId] = useState<string | null>(null);

  useEffect(() => {
    if (tab === 'Browse') {
      const timer = setTimeout(fetchPacks, 500);
      return () => clearTimeout(timer);
    }
  }, [query, tab]);

  const fetchPacks = async () => {
    setLoading(true);
    try {
      const res = await searchModpacks(query, 12);
      setModpacks(res.hits);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleInstall = (m: ModResult) => {
    setInstallingId(m.project_id);
    setTimeout(() => {
      installModpack(m);
      setInstallingId(null);
    }, 3000); // Simulate download
  };

  const isInstalled = (id: string) => installedModpacks.some(p => p.project_id === id);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="space-y-8 pt-4 pb-20"
    >
      {/* Search Header */}
      <div className="space-y-4">
        <div className="flex glass-dark p-1 rounded-pill max-w-sm">
          {['Browse', 'Installed', 'Mods'].map(t => (
            <button
              key={t}
              onClick={() => t === 'Mods' ? navigate('/mods') : setTab(t)}
              className={`flex-1 py-3 rounded-pill text-[10px] font-black uppercase tracking-widest transition-all ${tab === t ? 'bg-flux-gold text-bg-1 shadow-[0_0_15px_rgba(255,215,0,0.3)]' : 'text-white/30 hover:text-white'}`}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="relative group">
          <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none">
            {loading ? <Loader2 className="text-flux-gold animate-spin" size={18} /> : <Search className="text-white/20 group-focus-within:text-flux-gold transition-colors" size={18} />}
          </div>
          <input 
            type="text" 
            placeholder="DISCOVER CURATED MODPACKS..."
            className="w-full bg-white/3 border border-white/5 rounded-2xl py-4 pl-12 pr-4 text-sm font-display font-black uppercase tracking-widest focus:outline-none focus:border-flux-gold/50 focus:bg-white/5 transition-all"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <div className="absolute right-4 top-1/2 -translate-y-1/2 flex gap-2">
            <FluxChip label="FABRIC" className="bg-bg-1/50" />
            <FluxChip label="1.20.1" className="bg-bg-1/50" />
          </div>
        </div>
      </div>

      {tab === 'Browse' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {loading && modpacks.length === 0 ? (
            Array(4).fill(0).map((_, i) => (
              <div key={i} className="h-64 glass-dark rounded-2xl animate-pulse" />
            ))
          ) : (
            modpacks.map((m) => (
              <ModpackCard 
                key={m.project_id} 
                modpack={m} 
                isInstalled={isInstalled(m.project_id)}
                isInstalling={installingId === m.project_id}
                onInstall={handleInstall}
              />
            ))
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {installedModpacks.length === 0 ? (
             <FluxCard variant={GlassVariant.DARK} className="py-20 text-center border-dashed border-white/10">
                <Package size={48} className="text-white/5 mx-auto mb-4" />
                <p className="text-xs text-text-muted font-black uppercase tracking-widest">No modpacks installed in your library</p>
                <button onClick={() => setTab('Browse')} className="mt-4 text-flux-gold text-[10px] font-black uppercase tracking-[.2em] hover:brightness-125">Browse Modpacks <ChevronRight className="inline" size={12} /></button>
             </FluxCard>
          ) : (
            installedModpacks.map((m) => (
              <ModpackCard 
                key={m.project_id} 
                modpack={m} 
                isInstalled={true}
                isInstalling={false}
                onInstall={() => {}}
              />
            ))
          )}
        </div>
      )}

      {/* Recommended for performance */}
      <FluxCard variant={GlassVariant.GOLD} className="relative overflow-hidden group">
         <div className="absolute top-0 right-0 p-8 opacity-10 -rotate-12 transform group-hover:scale-110 transition-transform">
           <Zap size={100} />
         </div>
         <div className="relative z-10 space-y-2">
           <h3 className="text-bg-1 font-display font-black text-lg uppercase tracking-tight">OPTIMIZATION ENGINE</h3>
           <p className="text-[11px] text-bg-1/70 font-bold uppercase tracking-widest max-w-md">Our launcher automatically injects ASM transformations to ensure stable 60FPS even on medium-tier devices. Modpacks are fully optimized before first run.</p>
           <FluxButton label="TWEAK ENGINE" variant={GlassVariant.DARK} className="bg-bg-1 text-white border-none mt-4 text-[9px] px-6" />
         </div>
      </FluxCard>
    </motion.div>
  );
};

export default ModpacksScreen;
