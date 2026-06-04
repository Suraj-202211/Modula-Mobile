import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'motion/react';
import { Search, Grid, Download, Check, Loader2 } from 'lucide-react';
import { 
  FluxCard, 
  GlassVariant, 
  FluxChip, 
  FluxIconButton,
  FluxBadge,
  FluxButton,
  FluxTopBar,
  FluxDivider
} from '../components/glass/GlassComponents';
import { searchMods, ModResult } from '../services/modService';

import { useLauncherStore } from '../store/launcherStore';

const ModItem = React.memo(({ 
  mod, 
  isInstalled, 
  installingModId, 
  onInstall 
}: { 
  mod: ModResult, 
  isInstalled: boolean, 
  installingModId: string | null, 
  onInstall: (m: ModResult) => void 
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      className="h-full"
    >
      <FluxCard variant={GlassVariant.DARK} className="flex flex-col gap-3 h-full border-white/5 hover:border-flux-gold/20 transition-colors">
        <div className="w-12 h-12 bg-bg-4 rounded-xl flex items-center justify-center overflow-hidden border border-white/5">
          {mod.icon_url ? (
            <img src={mod.icon_url} alt={mod.title} className="w-full h-full object-cover" loading="lazy" />
          ) : (
            <Grid className="text-flux-gold" />
          )}
        </div>
        <div className="flex-grow">
          <h4 className="font-bold text-white leading-tight text-sm line-clamp-1">{mod.title}</h4>
          <p className="text-[10px] text-text-muted uppercase tracking-widest mt-1">by {mod.author}</p>
        </div>
        <div className="flex items-center justify-between mt-auto pt-2 border-t border-white/5">
          <span className="text-[10px] text-text-secondary flex items-center gap-1">
            <Download size={10} /> {(mod.downloads / 1000000).toFixed(1)}M
          </span>
          {isInstalled ? (
            <div className="w-8 h-8 bg-state-success/20 rounded-full flex items-center justify-center">
              <Check size={14} className="text-state-success" />
            </div>
          ) : installingModId === mod.project_id ? (
            <Loader2 size={16} className="text-flux-gold animate-spin" />
          ) : (
            <FluxIconButton icon={<Download size={14} />} size="SMALL" onClick={() => onInstall(mod)} />
          )}
        </div>
      </FluxCard>
    </motion.div>
  );
});

const ModsScreen: React.FC = () => {
  const navigate = useNavigate();
  const [tab, setTab] = useState('Browse');
  const [searchQuery, setSearchQuery] = useState('');
  const [mods, setMods] = useState<ModResult[]>([]);
  const [loading, setLoading] = useState(false);
  const { installedMods, installMod } = useLauncherStore();
  const [installingModId, setInstallingModId] = useState<string | null>(null);

  useEffect(() => {
    if (tab === 'Browse') {
      const delayDebounceFn = setTimeout(() => {
        handleSearch();
      }, 500);
      return () => clearTimeout(delayDebounceFn);
    }
  }, [searchQuery, tab]);

  const handleSearch = async () => {
    setLoading(true);
    try {
      const result = await searchMods(searchQuery, 20);
      setMods(result.hits);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleInstall = (mod: ModResult) => {
    setInstallingModId(mod.project_id);
    setTimeout(() => {
      installMod(mod);
      setInstallingModId(null);
    }, 2000);
  };

  const isInstalled = (id: string) => installedMods.some(m => m.project_id === id);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="space-y-6 pt-4 pb-12"
    >
      <div className="flex glass-dark p-1 rounded-pill">
        {['Browse', 'Installed', 'Modpacks'].map(t => (
          <button
            key={t}
            onClick={() => t === 'Modpacks' ? navigate('/modpacks') : setTab(t)}
            className={`flex-1 py-2 rounded-pill text-xs font-bold uppercase tracking-widest transition-all ${tab === t ? 'bg-flux-gold text-bg-1 shadow-[0_0_10px_rgba(255,215,0,0.3)]' : 'text-text-muted hover:text-white'}`}
          >
            {t}
          </button>
        ))}
      </div>

      <div className="relative group">
        <Search className={`absolute left-4 top-1/2 -translate-y-1/2 transition-colors ${loading ? 'text-flux-gold animate-spin' : 'text-text-muted group-focus-within:text-flux-gold'}`} size={18} />
        <input 
          type="text" 
          placeholder="Search Modrinth..."
          className="w-full glass-dark rounded-xl py-3 pl-12 pr-4 text-sm focus:outline-none focus:ring-1 focus:ring-flux-gold transition-all"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {tab === 'Browse' ? (
        <div className="grid grid-cols-2 gap-4">
          {loading && mods.length === 0 ? (
             Array(6).fill(0).map((_, i) => (
                <div key={i} className="h-44 glass-dark rounded-xl animate-pulse" />
             ))
          ) : (
            mods.map((mod) => (
              <ModItem 
                key={mod.project_id}
                mod={mod}
                isInstalled={isInstalled(mod.project_id)}
                installingModId={installingModId}
                onInstall={handleInstall}
              />
            ))
          )}
        </div>
      ) : (
        <div className="space-y-3">
          {installedMods.length === 0 ? (
            <div className="py-20 text-center glass-dark rounded-xl border border-dashed border-white/10">
               <p className="text-xs text-text-muted uppercase tracking-widest">No mods installed yet.</p>
            </div>
          ) : (
            installedMods.map((mod) => (
              <FluxCard key={mod.project_id} variant={GlassVariant.DARK} className="flex items-center gap-4 py-3">
                <div className="w-8 h-8 rounded-lg overflow-hidden shrink-0">
                  <img src={mod.icon_url} className="w-full h-full" alt="" loading="lazy" />
                </div>
                <div className="flex-grow">
                  <h4 className="font-bold text-white text-sm line-clamp-1">{mod.title}</h4>
                  <p className="text-[10px] text-text-muted uppercase tracking-widest">{mod.author} • v2.4.1</p>
                </div>
                <div className="w-10 h-10 bg-state-success/20 rounded flex items-center justify-center shrink-0">
                  <Check size={16} className="text-state-success" />
                </div>
              </FluxCard>
            ))
          )}
        </div>
      )}
    </motion.div>
  );
};

export default ModsScreen;
