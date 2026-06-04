import React, { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import { Layers, RefreshCw, Check } from 'lucide-react';
import { 
  FluxCard, 
  GlassVariant, 
  FluxChip, 
  FluxListTile, 
  FluxIconButton,
  FluxBadge,
  FluxProgressBar,
  FluxButton
} from '../components/glass/GlassComponents';
import { fetchMCVersions, MCVersion } from '../services/mcService';
import { useLauncherStore } from '../store/launcherStore';

const VersionsScreen: React.FC = () => {
  const [filter, setFilter] = useState('All');
  const [loader, setLoader] = useState('Vanilla');
  const [versions, setVersions] = useState<MCVersion[]>([]);
  const [loading, setLoading] = useState(true);
  const { setSelectedVersion, selectedVersion, addActivity } = useLauncherStore();
  const [downloadingVersion, setDownloadingVersion] = useState<string | null>(null);
  const [downloadProgress, setDownloadProgress] = useState(0);

  const handleDownload = (vId: string) => {
    setDownloadingVersion(vId);
    setDownloadProgress(0);
    
    let progress = 0;
    const interval = setInterval(() => {
      progress += 0.05 + Math.random() * 0.1;
      if (progress >= 1) {
        progress = 1;
        setDownloadProgress(1);
        clearInterval(interval);
        setTimeout(() => {
          setDownloadingVersion(null);
          addActivity({
            type: 'DOWNLOADED',
            versionId: vId,
            loader: 'Vanilla'
          });
        }, 800);
      } else {
        setDownloadProgress(progress);
      }
    }, 400);
  };

  useEffect(() => {
    fetchMCVersions().then(data => {
      setVersions(data.versions);
      setLoading(false);
    }).catch(err => {
      console.error(err);
      setLoading(false);
    });
  }, []);

  const filteredVersions = versions
    .filter(v => {
      if (filter === 'All') return true;
      if (filter === 'Release' && v.type === 'release') return true;
      if (filter === 'Snapshot' && v.type === 'snapshot') return true;
      if (filter === 'Alpha' && v.type === 'old_alpha') return true;
      if (filter === 'Beta' && v.type === 'old_beta') return true;
      return false;
    })
    .slice(0, 50);

  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      className="space-y-6 pt-4 pb-12"
    >
      <div className="flex flex-col gap-4 sticky top-14 z-20 bg-bg-1/80 backdrop-blur-md py-2 px-1">
        <div className="flex overflow-x-auto gap-2 pb-2 no-scrollbar">
          {['All', 'Release', 'Snapshot', 'Beta', 'Alpha'].map(f => (
            <FluxChip key={f} label={f} selected={filter === f} onClick={() => setFilter(f)} />
          ))}
        </div>
        <div className="flex overflow-x-auto gap-2 pb-2 no-scrollbar">
          {['Vanilla', 'Fabric', 'Forge', 'Quilt', 'NeoForge'].map(l => (
            <FluxChip key={l} label={l} selected={loader === l} onClick={() => setLoader(l)} />
          ))}
        </div>
      </div>

      <div className="space-y-3">
        {loading ? (
          Array(5).fill(0).map((_, i) => (
            <div key={i} className="h-20 glass-dark rounded-xl animate-pulse" />
          ))
        ) : (
          filteredVersions.map((v, i) => (
            <motion.div
              key={v.id}
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: (i % 10) * 0.05 }}
            >
              <FluxCard 
                variant={selectedVersion === v.id ? GlassVariant.GOLD : GlassVariant.DARK} 
                className={`p-0 overflow-hidden cursor-pointer ${downloadingVersion === v.id ? 'pointer-events-none' : ''}`}
                onClick={() => setSelectedVersion(v.id)}
              >
                <div className="flex items-center gap-4 p-4">
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center border ${selectedVersion === v.id ? 'bg-white/20 border-white/20' : 'bg-bg-4 border-white/5'}`}>
                    <Layers className={selectedVersion === v.id ? 'text-white' : 'text-flux-gold'} size={24} />
                  </div>
                  
                  <div className="flex-grow">
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-mono font-bold text-white tracking-tight">{v.id}</h3>
                      {v.type === 'release' && <FluxBadge label="STABLE" color={selectedVersion === v.id ? 'bg-white/20 text-white border-white/30' : 'bg-state-success/20 text-state-success border-state-success/30'} />}
                    </div>
                    <p className={`text-xs uppercase tracking-widest mt-0.5 ${selectedVersion === v.id ? 'text-white/70' : 'text-text-secondary'}`}>
                      {v.type.replace('_', ' ')} • {new Date(v.releaseTime).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="flex gap-2">
                    {downloadingVersion === v.id ? (
                      <div className="text-xs font-mono text-white/50">{Math.round(downloadProgress * 100)}%</div>
                    ) : selectedVersion === v.id ? (
                      <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
                        <Check size={20} className="text-white" />
                      </div>
                    ) : (
                      <FluxButton label="GET" variant="GHOST" className="h-9 px-4 text-xs" onClick={(e) => { e.stopPropagation(); handleDownload(v.id); }} />
                    )}
                  </div>
                </div>
                {downloadingVersion === v.id && (
                  <div className="px-4 pb-4">
                    <FluxProgressBar progress={downloadProgress} />
                  </div>
                )}
              </FluxCard>
            </motion.div>
          ))
        )}
      </div>
      
      <FluxButton 
        label="REFRESH VERSIONS" 
        variant="GHOST" 
        icon={<RefreshCw size={16} />}
        className="w-full border-dashed border-white/20 text-text-secondary py-6"
        onClick={() => {
           setLoading(true);
           fetchMCVersions().then(data => {
             setVersions(data.versions);
             setLoading(false);
           }).catch(err => {
             console.error(err);
             setLoading(false);
           });
        }}
      />
    </motion.div>
  );
};

export default VersionsScreen;
