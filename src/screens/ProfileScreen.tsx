import React, { useState, useRef } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  LogOut, 
  Shield, 
  Calendar, 
  Mail, 
  AlertTriangle, 
  User, 
  Upload, 
  Image as ImageIcon,
  Zap,
  Activity,
  Play
} from 'lucide-react';
import { 
  FluxCard, 
  GlassVariant, 
  FluxButton, 
  FluxBadge,
  FluxDivider
} from '../components/glass/GlassComponents';
import { SkinViewer } from '../components/glass/SkinViewer';
import { useLauncherStore } from '../store/launcherStore';

const ProfileScreen: React.FC = () => {
  const { 
    user, 
    logout, 
    customSkinUrl, 
    setCustomSkinUrl, 
    skinAnimation, 
    setSkinAnimation 
  } = useLauncherStore();
  
  const [showSignOutConfirm, setShowSignOutConfirm] = useState(false);
  const [showSkinEditor, setShowSkinEditor] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!user) return null;

  const formatDate = (timestamp: number) => {
    return new Intl.DateTimeFormat('en-US', { month: 'short', year: 'numeric' }).format(new Date(timestamp));
  };

  const isMicrosoft = user.type === 'MICROSOFT';

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        setCustomSkinUrl(event.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      className="space-y-6 pt-8 pb-12 overflow-visible"
    >
      {/* Top Profile Header */}
      <div className="flex flex-col items-center justify-center">
        <div className="relative">
          {isMicrosoft && (
            <motion.div 
              animate={{ rotate: 360 }}
              transition={{ duration: 12, repeat: Infinity, ease: 'linear' }}
              className="absolute -inset-3 rounded-full border border-dashed border-flux-gold/40"
            />
          )}
          <div className={`w-32 h-32 rounded-full p-1 shadow-[0_0_30px_rgba(255,215,0,0.2)] ${isMicrosoft ? 'border-2 border-flux-gold' : 'border-2 border-white/10'}`}>
            <img 
              src={isMicrosoft ? `https://mc-heads.net/avatar/${user.uuid}/96` : `https://mc-heads.net/avatar/${user.username}/96`} 
              alt="Avatar" 
              className="w-full h-full rounded-full bg-bg-2"
            />
          </div>
          <div className="absolute bottom-1 right-1 w-8 h-8 bg-state-success rounded-full border-4 border-bg-1 shadow-[0_0_10px_rgba(34,197,94,0.5)]" />
        </div>
        
        <h2 className="text-3xl font-display font-black text-white mt-8 uppercase tracking-tighter text-center">{user.username}</h2>
        <div className="flex gap-2 mt-2 justify-center">
          <FluxBadge 
            label={user.type} 
            className={isMicrosoft ? "bg-state-info/20 text-state-info border-state-info/40" : "bg-white/5 text-text-muted border-white/10"} 
          />
          {isMicrosoft && <FluxBadge label="PREMIUM" className="bg-flux-gold/20 text-flux-gold border-flux-gold/30" />}
        </div>
      </div>

      {/* Info Cards */}
      <div className="grid grid-cols-2 gap-4">
        <FluxCard variant={GlassVariant.DARK} className="p-4 flex flex-col gap-1 items-center text-center">
          <Calendar size={18} className="text-flux-gold mb-1" />
          <span className="text-[10px] text-text-muted uppercase tracking-widest font-black">Member Since</span>
          <span className="text-sm font-bold text-white uppercase tracking-tight">{formatDate(user.joinedDate)}</span>
        </FluxCard>
        <FluxCard variant={GlassVariant.DARK} className="p-4 flex flex-col gap-1 items-center text-center overflow-hidden">
          <Mail size={18} className="text-flux-gold mb-1" />
          <span className="text-[10px] text-text-muted uppercase tracking-widest font-black">Identity</span>
          <span className="text-sm font-bold text-white truncate w-full px-2" title={user.email}>
            {isMicrosoft ? user.email?.split('@')[0] : "LOCAL"}
          </span>
        </FluxCard>
      </div>

      {/* Skin Management Station */}
      <FluxCard variant={GlassVariant.DARK} className="p-0 overflow-hidden relative group">
        <div className="p-4 flex justify-between items-center bg-white/3 border-b border-white/5">
           <div className="flex items-center gap-2">
              <User size={14} className="text-flux-gold" />
              <span className="text-[10px] font-display font-bold text-white/50 uppercase tracking-[0.2em]">Skin Management Station</span>
           </div>
           <FluxButton 
             label={showSkinEditor ? "CLOSE" : "MANAGE"} 
             variant={GlassVariant.DARK} 
             size="SMALL" 
             className="h-6 text-[8px] px-3"
             onClick={() => setShowSkinEditor(!showSkinEditor)}
           />
        </div>

        <div className="h-80 relative bg-black/20">
          <SkinViewer 
            username={user.username} 
            customUrl={customSkinUrl}
            animation={skinAnimation}
            className="h-full border-none bg-transparent" 
          />
          
          <AnimatePresence>
            {showSkinEditor && (
              <motion.div 
                initial={{ opacity: 0, x: 50 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 50 }}
                className="absolute inset-y-0 right-0 w-48 bg-bg-1/90 backdrop-blur-xl border-l border-white/5 p-4 z-20 flex flex-col gap-4"
              >
                <div className="space-y-4">
                  <div className="space-y-2">
                    <span className="text-[8px] font-black text-white/30 uppercase tracking-[0.2em]">Simulation</span>
                    <div className="flex flex-col gap-2">
                      {(['standing', 'walking', 'running'] as const).map(anim => (
                        <button
                          key={anim}
                          onClick={() => setSkinAnimation(anim)}
                          className={`flex items-center justify-between px-3 py-2 rounded-lg text-[10px] font-black uppercase tracking-widest transition-all ${
                            skinAnimation === anim 
                              ? 'bg-flux-gold text-bg-1 shadow-[0_0_10px_rgba(255,215,0,0.3)]' 
                              : 'bg-white/5 text-white/40 hover:bg-white/10'
                          }`}
                        >
                          {anim}
                          {anim === 'running' ? <Zap size={10} /> : anim === 'walking' ? <Activity size={10} /> : <Play size={10} />}
                        </button>
                      ))}
                    </div>
                  </div>

                  <FluxDivider className="bg-white/5" />

                  <div className="space-y-2">
                    <span className="text-[8px] font-black text-white/30 uppercase tracking-[0.2em]">Direct Upload</span>
                    <input 
                      type="file" 
                      accept="image/*" 
                      className="hidden" 
                      ref={fileInputRef}
                      onChange={handleFileChange}
                    />
                    <button 
                      onClick={() => fileInputRef.current?.click()}
                      className="w-full flex items-center gap-2 px-3 py-3 rounded-xl bg-white/5 text-white hover:bg-white/10 border border-white/5 transition-all group"
                    >
                      <Upload size={14} className="text-flux-gold group-hover:scale-110 transition-transform" />
                      <span className="text-[9px] font-black uppercase tracking-widest">Select File</span>
                    </button>
                    {customSkinUrl && (
                      <button 
                        onClick={() => setCustomSkinUrl(null)}
                        className="w-full text-center py-2 text-state-error text-[8px] font-black uppercase tracking-widest hover:underline"
                      >
                        Reset to Default
                      </button>
                    )}
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </FluxCard>

      <div className="space-y-3">
        <FluxButton 
          label="SIGN OUT SESSION" 
          variant={GlassVariant.DARK}
          className="w-full border-state-error/30 text-state-error font-black tracking-[0.2em] h-14" 
          icon={<LogOut size={18} />} 
          onClick={() => setShowSignOutConfirm(true)} 
        />
      </div>

      <AnimatePresence>
        {showSignOutConfirm && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[200] bg-bg-1/95 backdrop-blur-3xl flex items-center justify-center p-6"
          >
            <FluxCard variant={GlassVariant.DARK} className="max-w-xs w-full p-8 text-center space-y-6 border-white/5">
               <div className="w-16 h-16 bg-state-error/10 rounded-full flex items-center justify-center mx-auto mb-4">
                 <AlertTriangle className="text-state-error" size={32} />
               </div>
               <div className="space-y-2">
                 <h2 className="text-xl font-display font-black text-white uppercase tracking-tight">De-Authorize?</h2>
                 <p className="text-[10px] text-text-muted uppercase tracking-widest font-bold leading-relaxed px-4">
                   Your authentication token will be revoked from the Flux engine.
                 </p>
               </div>
               <div className="flex flex-col gap-3">
                  <FluxButton label="REVOKE SESSION" variant={GlassVariant.DARK} className="text-state-error border-state-error/20" onClick={() => { logout(); setShowSignOutConfirm(false); }} />
                  <FluxButton label="KEEP SECURE" onClick={() => setShowSignOutConfirm(false)} />
               </div>
            </FluxCard>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default ProfileScreen;
