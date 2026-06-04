import React from 'react';
import { motion } from 'motion/react';
import { ChevronLeft, Download, FileJson, CheckCircle2, Gamepad2, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { FluxCard, GlassVariant, FluxButton, FluxSectionHeader } from '../components/glass/GlassComponents';
import { AppConfig } from '../config/AppConfig';
import { DownloadManager } from '../lib/distribution/DownloadManager';

const StepCard: React.FC<{ step: number; title: string; sub: string; action?: React.ReactNode; tip?: string }> = ({ step, title, sub, action, tip }) => (
  <FluxCard variant={GlassVariant.DARK} className="space-y-4 relative overflow-hidden">
    <div className="absolute top-0 right-0 p-8 opacity-[0.03] pointer-events-none">
       <span className="text-8xl font-display font-black italic">{step}</span>
    </div>
    <div className="flex gap-4">
       <div className="w-10 h-10 bg-flux-gold/10 rounded-xl flex items-center justify-center text-flux-gold shrink-0">
         <span className="font-display font-black">{step}</span>
       </div>
       <div className="space-y-1">
          <h3 className="text-sm font-display font-black text-white uppercase tracking-tight">{title}</h3>
          <p className="text-[10px] text-text-muted font-medium leading-relaxed">{sub}</p>
          {tip && (
            <div className="flex gap-2 items-center mt-2 p-2 bg-flux-gold/5 rounded-lg border border-flux-gold/10">
              <AlertCircle size={10} className="text-flux-gold" />
              <span className="text-[8px] text-flux-gold/70 uppercase font-black tracking-widest">{tip}</span>
            </div>
          )}
       </div>
    </div>
    {action && <div className="pt-2">{action}</div>}
  </FluxCard>
);

const InstallGuideScreen: React.FC = () => {
  const navigate = useNavigate();

  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      className="fixed inset-0 z-[100] bg-bg-1 flex flex-col p-6 overflow-y-auto"
    >
      <div className="flex items-center justify-between mb-8">
        <button onClick={() => navigate(-1)} className="p-2 hover:bg-white/5 rounded-full text-white/50">
          <ChevronLeft size={24} />
        </button>
        <h1 className="text-sm font-display font-black text-flux-gold uppercase tracking-[0.3em]">INSTALL GUIDE</h1>
        <div className="w-10" />
      </div>

      <div className="max-w-md mx-auto w-full space-y-6 pb-12">
        <div className="text-center space-y-2 mb-8">
          <h2 className="text-2xl font-display font-black text-white uppercase tracking-tighter">HOW TO INSTALL</h2>
          <p className="text-[10px] text-text-muted uppercase tracking-[0.3em] font-bold">Follow these steps carefully</p>
        </div>

        <StepCard 
          step={1}
          title="Download the APK"
          sub="Tap the button below to get the Modula Mobile installer from GitHub."
          action={
            <FluxButton 
              label="DOWNLOAD NOW" 
              className="w-full"
              onClick={() => DownloadManager.downloadAPK()}
            />
          }
        />

        <StepCard 
          step={2}
          title="Open the APK file"
          sub="Once downloaded, open your 'Downloads' folder or notification shade and tap the ModulaMobile.apk file."
        />

        <StepCard 
          step={3}
          title="Allow Installation"
          sub="If prompted by your browser, go to Settings → Apps → Special access → Install unknown apps → and toggle 'Allow' for your browser."
          tip="Usually found in 'Advanced' or 'Special' settings"
        />

        <StepCard 
          step={4}
          title="Launch Modula Mobile"
          sub="Open the app from your home screen, sign in, download your favorite version, and enjoy zero-stutter Minecraft!"
        />

        <FluxCard variant={GlassVariant.GOLD} className="border-none">
          <div className="flex gap-4">
            <CheckCircle2 size={24} className="text-bg-1 shrink-0" />
            <p className="text-[10px] text-bg-1 font-black uppercase tracking-widest leading-relaxed">
              Account data is synced via Microsoft/Mojang. Your local worlds may need manual backup if moving files.
            </p>
          </div>
        </FluxCard>
      </div>
    </motion.div>
  );
};

export default InstallGuideScreen;
