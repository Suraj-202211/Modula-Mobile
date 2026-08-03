import React, { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import { 
  Settings as SettingsIcon, 
  Cpu, 
  Database, 
  ExternalLink, 
  Github, 
  Globe, 
  MessageSquare, 
  ShieldCheck, 
  Smartphone, 
  Zap, 
  ChevronRight,
  LogOut,
  Trash2,
  AlertTriangle,
  Mic
} from 'lucide-react';
import { 
  FluxCard, 
  GlassVariant, 
  FluxButton, 
  FluxSectionHeader,
  FluxToggle,
  FluxBadge,
  FluxDivider,
  FluxSlider,
  FluxModal,
  FluxInput
} from '../components/glass/GlassComponents';
import { useLauncherStore } from '../store/launcherStore';
import { AppConfig } from '../config/AppConfig';
import { useTranslation } from '../lib/translations';

const SettingsScreen: React.FC = () => {
  const { settings, updateSettings, logout, user, resetSettingsToDefault } = useLauncherStore();
  const t_fn = useTranslation(settings.language);

  const [modalConfig, setModalConfig] = useState<{isOpen: boolean, type: 'CACHE' | 'LOGOUT' | 'IMPORT' | 'RESET' | null}>({isOpen: false, type: null});
  const [importValue, setImportValue] = useState('');
  
  const [isEditingJvm, setIsEditingJvm] = useState(false);
  const [tempJvm, setTempJvm] = useState(settings.jvmArgs);

  // Sync tempJvm if settings change externally
  useEffect(() => {
    setTempJvm(settings.jvmArgs);
  }, [settings.jvmArgs]);

  const closeModal = () => {
    setModalConfig({ isOpen: false, type: null });
    setImportValue(''); // reset
  };

  const handleConfirmAction = () => {
    switch (modalConfig.type) {
      case 'CACHE':
        closeModal();
        break;
      case 'IMPORT':
        if (importValue.trim().length > 0) {
          closeModal();
        }
        break;
      case 'LOGOUT':
        logout();
        closeModal();
        break;
      case 'RESET':
        resetSettingsToDefault();
        closeModal();
        break;
    }
  };

  const openModal = (type: 'CACHE' | 'LOGOUT' | 'IMPORT' | 'RESET') => {
    setModalConfig({ isOpen: true, type });
    if (type === 'IMPORT') {
      setImportValue('My Minecraft Launcher');
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-8 pb-12"
    >
      {/* Engine Configuration */}
      <FluxCard variant={GlassVariant.GOLD} className="relative overflow-hidden group border-flux-gold/30">
        <div className="absolute top-0 right-0 p-8 opacity-10 blur-xl group-hover:opacity-20 transition-opacity">
          <Zap size={120} fill="white" />
        </div>
        <FluxSectionHeader title={t_fn('engineMetrics')} className="mt-0" />
        <div className="space-y-6 relative z-10">
          <div className="space-y-3">
            <div className="flex justify-between items-center text-[10px] font-black uppercase tracking-widest text-white/80">
              <span className="flex items-center gap-2 text-flux-gold"><Cpu size={12} /> {t_fn('ramAllocation')}</span>
              <span className="text-flux-gold">{settings.ramAllocation}MB</span>
            </div>
            <input 
              type="range" 
              min="512" 
              max="16384" 
              step="512"
              value={settings.ramAllocation ?? 4096}
              onChange={(e) => updateSettings({ ramAllocation: parseInt(e.target.value) })}
              className="w-full accent-flux-gold h-1.5 bg-white/10 rounded-full appearance-none cursor-pointer"
            />
            <div className="flex justify-between text-[8px] font-bold text-white/40 uppercase tracking-[0.2em]">
              <span>512MB</span>
              <span className="text-flux-gold/40">RECOMMENDED: 4096MB</span>
              <span>16GB</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <FluxToggle 
              label="Unlock FPS" 
              checked={settings.fpsUnlock} 
              onCheckedChange={(v) => updateSettings({ fpsUnlock: v })} 
            />
            <FluxToggle 
              label="Rich Presence" 
              checked={settings.richPresence} 
              onCheckedChange={(v) => updateSettings({ richPresence: v })}
            />
          </div>

          <FluxDivider className="bg-white/5" />

          <div className="flex items-center justify-between gap-4 p-4 bg-white/5 rounded-xl border border-white/5">
            <div className="space-y-1">
              <div className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-white">
                <Zap size={14} className="text-flux-gold animate-pulse" />
                Performance Mode
              </div>
              <p className="text-[8px] text-white/50 font-bold uppercase tracking-widest leading-normal max-w-[180px]">
                Reduces UI animation density and optimizes JVM thread priority for minimum lag.
              </p>
            </div>
            <FluxToggle 
              label=""
              checked={settings.performanceMode}
              onCheckedChange={(v) => updateSettings({ performanceMode: v })}
            />
          </div>
        </div>
      </FluxCard>

      {/* Interface Customization */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title={t_fn('interfaceTheme')} className="mt-0" />
        <div className="grid grid-cols-2 md:grid-cols-5 gap-2">
          {(['DEFAULT', 'ONYX', 'VOLCANIC', 'NEON', 'ARCTIC'] as const).map((t) => (
            <button
              key={t}
              onClick={() => updateSettings({ theme: t })}
              className={`p-3 rounded-xl border transition-all duration-300 text-center space-y-1 ${
                settings.theme === t 
                  ? 'bg-flux-gold/20 border-flux-gold text-flux-gold' 
                  : 'bg-white/5 border-white/5 text-text-muted hover:bg-white/10'
              }`}
            >
              <div className="text-[10px] font-black uppercase tracking-tight">{t}</div>
            </button>
          ))}
        </div>
      </FluxCard>

      {/* Visual Settings */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-6">
        <FluxSectionHeader title={t_fn('visualFidelity')} className="mt-0" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2">
          <FluxSlider 
            label="Particle Density" 
            value={settings.particleIntensity ?? 60} 
            onChange={(v) => updateSettings({ particleIntensity: v })} 
            valueLabel={`${settings.particleIntensity ?? 60}%`}
          />
          <FluxSlider 
            label="Motion Interpolation" 
            value={settings.motionBlur ?? 80} 
            onChange={(v) => updateSettings({ motionBlur: v })} 
            valueLabel={`${settings.motionBlur ?? 80}%`}
          />
          <FluxSlider 
            label="UI Transparency" 
            value={settings.transparencyLevel ?? 10} 
            onChange={(v) => updateSettings({ transparencyLevel: v })} 
            valueLabel={`${settings.transparencyLevel ?? 10}%`}
          />
          <FluxSlider 
            label="UI Scaling" 
            value={settings.uiScaling ?? 100} 
            onChange={(v) => updateSettings({ uiScaling: v })} 
            valueLabel={`${settings.uiScaling ?? 100}%`}
          />
          
          <div className="flex flex-col gap-2 pt-2 border-t border-white/5 md:border-none col-span-full grid grid-cols-2 gap-4">
            <FluxToggle 
              label="Bloom Effects" 
              checked={settings.bloomEffect ?? true} 
              onCheckedChange={(v) => updateSettings({ bloomEffect: v })} 
            />
            <FluxToggle 
              label="Dynamic Shadows" 
              checked={settings.dynamicShadows ?? true} 
              onCheckedChange={(v) => updateSettings({ dynamicShadows: v })} 
            />
            <FluxToggle 
              label="Auto-Update" 
              checked={settings.autoUpdate ?? true} 
              onCheckedChange={(v) => updateSettings({ autoUpdate: v })} 
            />
            <FluxToggle 
              label="Show FPS Counter" 
              checked={settings.showFPS ?? false} 
              onCheckedChange={(v) => updateSettings({ showFPS: v })} 
            />
            <FluxToggle 
              label="Enable Snapshots" 
              checked={settings.enableSnapshots ?? false} 
              onCheckedChange={(v) => updateSettings({ enableSnapshots: v })} 
            />
            <FluxToggle 
              label="Advanced Debug" 
              checked={settings.debugLogs ?? false} 
              onCheckedChange={(v) => updateSettings({ debugLogs: v })} 
            />
            <FluxToggle 
              label="Battery Saver" 
              checked={settings.batterySaver ?? false} 
              onCheckedChange={(v) => updateSettings({ batterySaver: v })} 
            />
            <FluxToggle 
              label="GPU Acceleration" 
              checked={settings.gpuAcceleration ?? true} 
              onCheckedChange={(v) => updateSettings({ gpuAcceleration: v })} 
            />
          </div>
        </div>
      </FluxCard>

      {/* Preferences Section */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title={t_fn('applicationPreferences')} className="mt-0" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
           <div className="space-y-2">
              <label className="text-[10px] font-black uppercase tracking-widest text-text-muted">{t_fn('interfaceLanguage')}</label>
              <select 
                value={settings.language ?? 'English (US)'}
                onChange={(e) => updateSettings({ language: e.target.value })}
                className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-[10px] font-black uppercase tracking-widest text-white appearance-none outline-none focus:border-flux-gold transition-colors"
              >
                <option value="English (US)">English (US)</option>
                <option value="Spanish">Spanish</option>
                <option value="French">French</option>
                <option value="Hindi">Hindi</option>
                <option value="Malayalam">Malayalam (മലയാളം)</option>
              </select>
           </div>
        </div>
      </FluxCard>

      {/* Audio & Voice Section */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title={t_fn('fluxVoice')} className="mt-0" />
        <div className="space-y-4">
           <div className="p-4 bg-white/5 rounded-xl border border-white/5 flex items-center justify-between">
              <div className="flex items-center gap-3">
                 <Mic size={20} className={settings.fluxVoiceEnabled ? "text-flux-gold" : "text-white/20"} />
                 <div className="space-y-0.5">
                    <p className="text-[10px] font-black uppercase tracking-tight text-white">{t_fn('fluxVoice')}</p>
                    <p className="text-[8px] text-white/40 uppercase font-black tracking-widest leading-tight">{t_fn('voiceDescription')}</p>
                 </div>
              </div>
              <FluxToggle 
                label="" 
                checked={settings.fluxVoiceEnabled ?? true} 
                onCheckedChange={(v) => updateSettings({ fluxVoiceEnabled: v })} 
              />
           </div>

           {settings.fluxVoiceEnabled && (
             <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FluxToggle 
                  label="Proximity Chat" 
                  checked={settings.fluxVoiceProximity ?? true} 
                  onCheckedChange={(v) => updateSettings({ fluxVoiceProximity: v })} 
                />
                <FluxToggle 
                  label="Noise Suppression" 
                  checked={settings.fluxVoiceNoiseSuppression ?? true} 
                  onCheckedChange={(v) => updateSettings({ fluxVoiceNoiseSuppression: v })} 
                />
                <FluxToggle 
                  label="High Bitrate (HD)" 
                  checked={settings.fluxVoiceHighBitrate ?? true} 
                  onCheckedChange={(v) => updateSettings({ fluxVoiceHighBitrate: v })} 
                />
             </div>
           )}
        </div>
      </FluxCard>

      {/* Migration Tools */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title={t_fn('migrationTools')} className="mt-0" />
        <div className="p-4 bg-white/5 rounded-xl border border-white/5 flex items-center justify-between">
           <div className="flex items-center gap-3">
              <Database size={20} className="text-flux-gold" />
              <div className="space-y-0.5">
                 <p className="text-[10px] font-black uppercase tracking-tight text-white">{t_fn('migrationTools')}</p>
                 <p className="text-[8px] text-white/40 uppercase font-black tracking-widest leading-tight">Import data from Pojav, Prism or SKLauncher</p>
              </div>
           </div>
           <FluxButton 
             variant="GHOST" 
             label="IMPORT" 
             size="SMALL" 
             className="text-[8px] h-8 px-4"
             onClick={() => openModal('IMPORT')} 
           />
        </div>
      </FluxCard>

      {/* Experimental Features */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title="System & Experimental" className="mt-0" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
           <div className="p-4 bg-white/5 rounded-xl border border-white/5 flex items-center justify-between">
              <div className="flex items-center gap-3">
                 <Smartphone size={20} className="text-flux-gold" />
                 <div className="space-y-0.5">
                    <p className="text-[10px] font-black uppercase tracking-widest text-white">Touch Haptics</p>
                    <p className="text-[8px] text-white/40 uppercase font-black tracking-widest">Tactile navigation feedback</p>
                 </div>
              </div>
              <FluxToggle label="" checked={settings.touchHaptics ?? true} onCheckedChange={(v) => updateSettings({ touchHaptics: v })} />
           </div>
          <div className="p-4 bg-white/5 rounded-xl border border-white/5 flex items-center justify-between">
              <div className="flex items-center gap-3">
                 <ShieldCheck size={20} className="text-flux-gold" />
                 <div className="space-y-0.5">
                    <p className="text-[10px] font-black uppercase tracking-tight text-white">Secure Boot</p>
                    <p className="text-[8px] text-white/40 uppercase font-black tracking-widest">Verify file integrity on start</p>
                 </div>
              </div>
              <FluxToggle label="" checked={settings.secureBoot ?? true} onCheckedChange={(v) => updateSettings({ secureBoot: v })} />
           </div>
        </div>
      </FluxCard>

      {/* JVM Arguments */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title="JVM Parameters" className="mt-0" />
        
        {isEditingJvm ? (
          <div className="space-y-3">
            <textarea
              value={tempJvm}
              onChange={(e) => setTempJvm(e.target.value)}
              className="w-full h-24 bg-black/40 border border-flux-gold/50 rounded-xl p-4 font-mono text-[10px] text-white/90 outline-none resize-none transition-colors"
              autoFocus
            />
            <div className="flex gap-2 justify-end">
              <FluxButton 
                label="CANCEL" 
                variant="GHOST" 
                size="SMALL" 
                onClick={() => {
                  setTempJvm(settings.jvmArgs);
                  setIsEditingJvm(false);
                }} 
              />
              <FluxButton 
                label="SAVE" 
                size="SMALL" 
                onClick={() => {
                  updateSettings({ jvmArgs: tempJvm });
                  setIsEditingJvm(false);
                }} 
              />
            </div>
          </div>
        ) : (
          <div className="p-4 bg-black/40 rounded-xl border border-white/5 font-mono text-[9px] text-white/50 break-all leading-relaxed relative group">
            <p className="pr-10">{settings.jvmArgs}</p>
            <button 
              className="absolute top-4 right-4 p-2 bg-white/5 hover:bg-flux-gold/20 hover:text-flux-gold rounded-lg opacity-0 group-hover:opacity-100 transition-all"
              onClick={() => setIsEditingJvm(true)}
            >
              <SettingsIcon size={12} />
            </button>
          </div>
        )}
        <p className="text-[8px] text-text-muted uppercase tracking-widest font-bold">Optimized for ASM injection</p>
      </FluxCard>

      {/* About Section */}
      <FluxCard variant={GlassVariant.DARK} className="space-y-4">
        <FluxSectionHeader title="About Modula Mobile" className="mt-0" />
        <div className="flex items-center justify-between">
           <div className="flex items-center gap-3">
              <div className="p-2 bg-flux-gold/20 rounded-xl">
                 <Zap className="text-flux-gold" size={24} />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="font-display font-black text-white uppercase tracking-tight">MODULA MOBILE</h3>
                  <FluxBadge label={`V${AppConfig.appVersion}`} className="bg-flux-gold text-bg-1 border-none text-[8px]" />
                </div>
                <p className="text-[9px] text-text-muted font-bold uppercase tracking-widest">Build {AppConfig.buildDate}</p>
              </div>
           </div>
        </div>

        <div className="grid grid-cols-2 gap-3 mt-4">
          <FluxButton 
            variant="GHOST" 
            label="WEBSITE" 
            size="SMALL"
            className="text-[9px] border-white/10 text-white/70"
            onClick={() => window.open(AppConfig.website, '_blank')}
          />
          <FluxButton 
            variant="GHOST" 
            label="RELEASES" 
            size="SMALL"
            className="text-[9px] border-white/10 text-white/70"
            onClick={() => window.open(AppConfig.releasesUrl, '_blank')}
          />
          <FluxButton 
            variant="GHOST" 
            label="DISCORD" 
            size="SMALL"
            className="text-[9px] border-white/10 text-white/70"
            onClick={() => window.open(AppConfig.discordInvite, '_blank')}
          />
          <FluxButton 
            variant="GHOST" 
            label="GITHUB" 
            size="SMALL"
            className="text-[9px] border-white/10 text-white/70"
            onClick={() => window.open(`https://github.com/${AppConfig.githubUsername}/${AppConfig.githubRepo}`, '_blank')}
          />
        </div>

        <FluxDivider />
        
        <div className="space-y-4">
           <div className="flex justify-between items-center text-[10px] text-white/50 uppercase font-black tracking-widest">
              <span>Legal & Credits</span>
              <ShieldCheck size={14} className="text-flux-gold opacity-50" />
           </div>
           
           <p className="text-[8px] text-text-muted leading-relaxed uppercase font-bold tracking-widest opacity-60">
             This UI is a React-based Front-end Reference for mobile launchers.
             All game rights belong to Mojang AB. Use responsibly.
           </p>

           <div className="p-4 bg-white/3 rounded-xl border border-white/5 space-y-2">
              <p className="text-[8px] text-white/30 uppercase font-black tracking-widest leading-none">OFFICIAL MODULA MOBILE EDITION</p>
              <p className="text-[8px] text-white/30 uppercase font-black tracking-widest leading-none">DESIGNED BY MODULAMC TEAM</p>
           </div>
        </div>
      </FluxCard>

      {/* Account & System Actions */}
      <div className="space-y-3 pt-4">
        <FluxButton 
          variant="GHOST"
          label="CLEAR GAME CACHE" 
          className="w-full border-white/10 text-white/50 hover:text-white hover:border-white/30 font-black tracking-[0.2em]"
          onClick={() => openModal('CACHE')}
        />
        <FluxButton 
          variant="GHOST"
          label="RESET SETTINGS TO DEFAULT" 
          className="w-full border-state-error/30 text-state-error hover:bg-state-error/10 font-black tracking-[0.2em]"
          onClick={() => openModal('RESET')}
        />
        <FluxButton 
          variant="DANGER"
          label="LOGOUT SESSION" 
          className="w-full font-black tracking-[0.2em]"
          onClick={() => openModal('LOGOUT')}
        />
      </div>

      {/* Modals */}
      <FluxModal
        isOpen={modalConfig.isOpen}
        onClose={closeModal}
        title={
          modalConfig.type === 'CACHE' ? 'Clear Game Cache' :
          modalConfig.type === 'IMPORT' ? 'Import Launcher Data' :
          modalConfig.type === 'LOGOUT' ? 'Logout Session' :
          modalConfig.type === 'RESET' ? 'Reset Settings' : ''
        }
        actions={
          <>
            <FluxButton label="CANCEL" variant="GHOST" size="SMALL" onClick={closeModal} />
            <FluxButton 
              label={
                modalConfig.type === 'CACHE' ? 'CLEAR' :
                modalConfig.type === 'IMPORT' ? 'IMPORT' :
                modalConfig.type === 'LOGOUT' ? 'LOGOUT' :
                modalConfig.type === 'RESET' ? 'RESET ALL' : 'CONFIRM'
              } 
              variant={
                (modalConfig.type === 'LOGOUT' || modalConfig.type === 'RESET') ? 'DANGER' : 'PRIMARY'
              } 
              size="SMALL" 
              onClick={handleConfirmAction}
              className={
                modalConfig.type === 'IMPORT' && importValue.trim().length === 0 
                  ? 'opacity-50 pointer-events-none' 
                  : ''
              }
            />
          </>
        }
      >
        {modalConfig.type === 'CACHE' && (
          <div className="flex items-start gap-3">
            <AlertTriangle className="text-flux-amber shrink-0" />
            <p>Are you sure you want to clear game cache? This will free up storage space but you may experience longer load times initially. This will <strong>NOT</strong> delete worlds or screenshots.</p>
          </div>
        )}
        
        {modalConfig.type === 'IMPORT' && (
          <div className="space-y-4">
            <p>Enter the name of the launcher you want to import data from (e.g., Pojav, Prism, SKLauncher):</p>
            <FluxInput 
              value={importValue}
              onChange={setImportValue}
              placeholder="e.g. Prism Launcher"
              autoFocus={true}
              onEnter={importValue.trim().length > 0 ? handleConfirmAction : undefined}
              onEscape={closeModal}
            />
          </div>
        )}

        {modalConfig.type === 'LOGOUT' && (
          <div className="flex items-start gap-3">
            <LogOut className="text-state-error shrink-0" />
            <p>Log out from your current Minecraft session? You will need to re-authenticate to play online servers.</p>
          </div>
        )}

        {modalConfig.type === 'RESET' && (
          <div className="flex items-start gap-3">
            <AlertTriangle className="text-state-error shrink-0" />
            <p>Are you absolutely sure? This will wipe all your custom configurations, including JVM arguments, RAM allocation, and visual settings, restoring everything to a clean slate.</p>
          </div>
        )}
      </FluxModal>

    </motion.div>
  );
};

export default SettingsScreen;
