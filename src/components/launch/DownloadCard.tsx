import React, { useEffect, useState } from 'react';
import { AppConfig } from '../../config/AppConfig';
import { DownloadManager, ReleaseInfo } from '../../lib/distribution/DownloadManager';
import { 
  FluxCard, 
  GlassVariant, 
  FluxButton, 
  FluxDivider, 
  FluxBadge 
} from '../glass/GlassComponents';
import { Download, Zap, Gamepad2, ShieldCheck, Box, MessageSquare } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const ModulaLogo: React.FC<{ size?: number }> = ({ size = 32 }) => (
  <div 
    className="bg-flux-gold rounded-lg flex items-center justify-center text-bg-1 font-display font-black italic shadow-[0_0_15px_rgba(255,215,0,0.4)]"
    style={{ width: size, height: size, fontSize: size * 0.6 }}
  >
    M
  </div>
);

const FeatureRow: React.FC<{ icon: React.ReactNode; text: string }> = ({ icon, text }) => (
  <div className="flex items-center gap-3 text-[10px] text-white/70 uppercase tracking-widest font-bold">
    <div className="text-flux-gold">{icon}</div>
    {text}
  </div>
);

const DownloadCard: React.FC<{ onCopyCommand: () => void }> = ({ onCopyCommand }) => {
  const [release, setRelease] = useState<ReleaseInfo | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    DownloadManager.fetchLatestRelease().then(setRelease);
  }, []);

  const handleDownload = () => {
    if (DownloadManager.isAndroid()) {
      navigate('/install-guide');
    } else {
      DownloadManager.downloadAPK();
    }
  };

  const fileSize = release ? (release.size / (1024 * 1024)).toFixed(1) : '32.0';

  return (
    <FluxCard variant={GlassVariant.GOLD} className="space-y-4 border-flux-gold/30">
      <div className="flex items-center gap-4">
        <ModulaLogo size={40} />
        <div>
          <h2 className="text-sm font-display font-black text-bg-1 uppercase tracking-tight">GET MODULA MOBILE</h2>
          <p className="text-[10px] text-bg-1/60 font-bold uppercase tracking-widest leading-none">Native Android app — plays real Minecraft</p>
        </div>
      </div>

      <FluxDivider className="bg-bg-1/10" />

      <div className="grid grid-cols-1 gap-3 px-1">
        <FeatureRow icon={<Zap size={14} fill="currentColor" />} text="Real Minecraft Java Edition" />
        <FeatureRow icon={<Gamepad2 size={14} />} text="Golden Flux Engine — 144Hz UI" />
        <FeatureRow icon={<Zap size={14} />} text="Unlimited FPS · Zero Stutter" />
        <FeatureRow icon={<ShieldCheck size={14} />} text="Hardware-backed Security" />
        <FeatureRow icon={<Box size={14} />} text="Fabric · Forge · Quilt · NeoForge" />
      </div>

      <FluxDivider className="bg-bg-1/10" />

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <FluxBadge label="LATEST" className="bg-bg-1 text-flux-gold border-none" />
          <span className="text-[10px] font-mono font-black text-bg-1">{release?.tagName || AppConfig.appVersion}</span>
        </div>
        <span className="text-[9px] font-bold text-bg-1/40 uppercase tracking-widest">
          {fileSize}MB · Android 8.0+
        </span>
      </div>

      <FluxButton
        label="⬇ DOWNLOAD MODULA MOBILE"
        onClick={handleDownload}
        className="w-full bg-bg-1 text-flux-gold shadow-xl"
      />

      <div className="grid grid-cols-2 gap-3">
        <FluxButton
          variant={GlassVariant.DARK}
          label="ALL RELEASES"
          className="text-[9px] bg-bg-1/10 border-bg-1/20 text-bg-1"
          onClick={() => window.open(AppConfig.releasesUrl, '_blank')}
        />
        <FluxButton
          variant={GlassVariant.DARK}
          label="COPY COMMAND"
          className="text-[9px] bg-bg-1/10 border-bg-1/20 text-bg-1"
          onClick={onCopyCommand}
        />
      </div>

      <FluxCard variant={GlassVariant.DARK} className="bg-bg-1/5 border-bg-1/10 p-3 mt-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <MessageSquare size={16} className="text-[#5865F2]" />
            <span className="text-[9px] font-bold text-bg-1 uppercase tracking-widest">Need help? Join Discord</span>
          </div>
          <FluxButton
            label="JOIN"
            size="SMALL"
            variant={GlassVariant.DARK}
            className="text-[8px] h-6 px-3 bg-bg-1 text-flux-gold border-none"
            onClick={() => window.open(AppConfig.discordInvite, '_blank')}
          />
        </div>
      </FluxCard>
    </FluxCard>
  );
};

export default DownloadCard;
