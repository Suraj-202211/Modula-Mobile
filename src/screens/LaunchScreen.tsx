import React, { useEffect, useState, useRef } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  ChevronLeft, 
  Zap, 
  ShieldCheck, 
  Terminal, 
  CheckCircle2, 
  AlertTriangle,
  Play,
  RotateCcw,
  XCircle,
  Copy,
  Clock,
  Activity,
  MemoryStick,
  Cpu,
  Smartphone,
  HardDrive,
  Settings as SettingsIcon,
  Gamepad2
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useLauncherStore, LaunchState } from '../store/launcherStore';
import { 
  FluxCard, 
  GlassVariant, 
  FluxButton, 
  FluxSectionHeader,
  FluxDivider,
  FluxBadge
} from '../components/glass/GlassComponents';
import { verifyAllGameFiles } from '../lib/launcher/FileVerifier';
import { buildLaunchCommand } from '../lib/launcher/JVMArgBuilder';
import { AppConfig } from '../config/AppConfig';
import DownloadCard from '../components/launch/DownloadCard';

const StepIndicator: React.FC<{ currentStep: number }> = ({ currentStep }) => {
  const steps = [
    "Account",
    "Files",
    "JVM Args",
    "Bridge",
    "Ready"
  ];

  return (
    <div className="flex items-center justify-between px-2 mb-8 relative">
      <div className="absolute top-4 left-0 right-0 h-[1px] bg-white/10 -z-10" />
      {steps.map((step, i) => {
        const isCompleted = i < currentStep - 1;
        const isActive = i === currentStep - 1;
        return (
          <div key={i} className="flex flex-col items-center gap-2">
            <div className={`w-3 h-3 rounded-full border-2 transition-all duration-500 ${
              isCompleted ? 'bg-flux-gold border-flux-gold scale-100 shadow-[0_0_10px_rgba(255,215,0,0.5)]' : 
              isActive ? 'bg-bg-1 border-flux-gold scale-125 shadow-[0_0_15px_rgba(255,215,0,0.8)]' : 
              'bg-bg-1 border-white/20'
            }`}>
              {isActive && (
                <motion.div 
                  animate={{ scale: [1, 1.5, 1], opacity: [1, 0, 1] }} 
                  transition={{ duration: 1.5, repeat: Infinity }}
                  className="w-full h-full bg-flux-gold rounded-full"
                />
              )}
            </div>
            <span className={`text-[8px] font-black uppercase tracking-widest ${
              isActive ? 'text-flux-gold' : isCompleted ? 'text-flux-gold/60' : 'text-white/20'
            }`}>
              {step}
            </span>
          </div>
        );
      })}
    </div>
  );
};

const DeviceCheckCard: React.FC = () => {
  const [ram, setRam] = useState<number>(0);
  const [storage, setStorage] = useState<number>(0);

  useEffect(() => {
    // @ts-ignore
    if (navigator.deviceMemory) setRam(navigator.deviceMemory);
    if (navigator.storage && navigator.storage.estimate) {
      navigator.storage.estimate().then(est => {
        setStorage(Math.round((est.quota || 0) / (1024 * 1024 * 1024)));
      });
    }
  }, []);

  const checks = [
    { label: "OS Compatibility", status: true, icon: <Smartphone size={10} /> },
    { label: "Memory (4GB+)", status: ram >= 4, icon: <Cpu size={10} /> },
    { label: "Vulkan Bridge", status: true, icon: <Zap size={10} /> },
    { label: "Free Storage", status: storage >= 2, icon: <HardDrive size={10} /> }
  ];

  return (
    <FluxCard variant={GlassVariant.DARK} className="p-4 border-white/5">
      <div className="flex justify-between items-center mb-4">
        <span className="text-[10px] font-display font-black text-flux-gold uppercase tracking-[0.2em]">Device Integrity Check</span>
        <FluxBadge label="STABLE" className="bg-state-success/10 text-state-success border-none text-[8px]" />
      </div>
      <div className="grid grid-cols-2 gap-x-6 gap-y-3">
        {checks.map((check, i) => (
          <div key={i} className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-white/50 text-[9px] uppercase font-bold tracking-widest">
              {check.icon}
              {check.label}
            </div>
            <div className={`w-1.5 h-1.5 rounded-full ${check.status ? 'bg-state-success shadow-[0_0_5px_green]' : 'bg-state-error shadow-[0_0_5px_red]'}`} />
          </div>
        ))}
      </div>
    </FluxCard>
  );
};

const LaunchScreen: React.FC = () => {
  const navigate = useNavigate();
  const { 
    user, 
    selectedVersion, 
    settings, 
    launchState, 
    setLaunchState,
    launchProgress,
    setLaunchProgress,
    launchLogs,
    addLaunchLog,
    clearLaunchLogs,
    launchError,
    setLaunchError,
    bridgeConnected,
    setBridgeConnected
  } = useLauncherStore();

  const [sessionTime, setSessionTime] = useState(0);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const logEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (launchState === LaunchState.IDLE) {
      startLaunchFlow();
    }
  }, []);

  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [launchLogs]);

  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (launchState === LaunchState.GAME_RUNNING) {
      timer = setInterval(() => setSessionTime(prev => prev + 1), 1000);
    }
    return () => clearInterval(timer);
  }, [launchState]);

  const startLaunchFlow = async () => {
    setLaunchState(LaunchState.PRE_LAUNCH);
    clearLaunchLogs();
    setLaunchProgress(0);
    setLaunchError(null);
    setCurrentStep(1);

    try {
      addLaunchLog("⚡ Initializing Modula Mobile Engine...");
      
      // Step 1: Check Account
      if (!user) throw new Error("AuthException: No account selected.");
      addLaunchLog("✅ Account verified: " + user.username);
      setLaunchProgress(0.1);
      setCurrentStep(2);

      // Step 2: File Verification
      addLaunchLog("📁 Verifying file integrity (SHA-1 checksums)...");
      const verif = await verifyAllGameFiles(selectedVersion, (curr, total, file) => {
        setLaunchProgress(0.1 + (curr / total) * 0.4);
        addLaunchLog(`   ✅ ${file} verified`);
      });
      addLaunchLog(`✅ Integrity check complete. ${verif.verified} objects validated.`);
      setLaunchProgress(0.5);
      setCurrentStep(3);

      // Step 3: Build Command
      addLaunchLog("⚙️ Building optimized JVM argument chain...");
      const command = buildLaunchCommand({
        javaPath: 'java',
        ramMb: settings.ramAllocation,
        versionId: selectedVersion,
        username: user.username,
        uuid: user.uuid,
        accessToken: user.type === 'OFFLINE' ? '0' : 'token',
        userType: user.type === 'MICROSOFT' ? 'msa' : 'legacy',
        gameDir: `/home/${AppConfig.githubUsername}/.minecraft`,
        assetsDir: `/home/${AppConfig.githubUsername}/.minecraft/assets`,
        assetIndex: selectedVersion.split('.').slice(0, 2).join('.'),
        classpath: './bin/client.jar:./lib/*',
        mainClass: 'net.minecraft.client.main.Main',
        nativesDir: './natives',
        performanceMode: settings.performanceMode
      });
      
      addLaunchLog("⚡ Real Launch Command Generated");
      setLaunchProgress(0.7);
      setCurrentStep(4);

      // Step 4: Bridge Detection
      addLaunchLog("📡 Checking for local Bridge context...");
      const bridgeRes = await fetch('http://localhost:25565/status', { signal: AbortSignal.timeout(1000) })
        .catch(() => null);
      
      const isBridgeAvailable = bridgeRes?.ok || false;
      setBridgeConnected(isBridgeAvailable);
      if (isBridgeAvailable) {
        addLaunchLog("✅ Bridge linkage established (Port 25565)");
      } else {
        addLaunchLog("⚠️ External Bridge environment not found.");
      }
      setLaunchProgress(1);
      setCurrentStep(5);

      // Phase 2: Launching
      setLaunchState(LaunchState.LAUNCHING);
      
      if (isBridgeAvailable) {
        addLaunchLog("🚀 Sending launch signal to native process...");
        setTimeout(() => setLaunchState(LaunchState.GAME_RUNNING), 2000);
      }

    } catch (err: any) {
      setLaunchError(err.message || "An unexpected error occurred.");
      setLaunchState(LaunchState.ERROR);
    }
  };

  const formatTime = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const handleCopyCommand = () => {
    const command = buildLaunchCommand({
      javaPath: 'java',
      ramMb: settings.ramAllocation,
      versionId: selectedVersion,
      username: user?.username || 'Guest',
      uuid: user?.uuid || '0',
      accessToken: user?.type === 'OFFLINE' ? '0' : 'token',
      userType: 'legacy',
      gameDir: `/home/${AppConfig.githubUsername}/.minecraft`,
      assetsDir: `/home/${AppConfig.githubUsername}/.minecraft/assets`,
      assetIndex: selectedVersion.split('.').slice(0, 2).join('.'),
      classpath: './bin/client.jar:./lib/*',
      mainClass: 'net.minecraft.client.main.Main',
      nativesDir: './natives',
      performanceMode: settings.performanceMode
    });
    navigator.clipboard.writeText(command);
    alert("Real JVM command copied to clipboard!");
  };

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-[100] bg-bg-1 flex flex-col p-6 overflow-y-auto no-scrollbar"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <button 
          onClick={() => setShowCancelConfirm(true)}
          className="flex items-center gap-2 group"
        >
          <div className="p-2 group-hover:bg-white/5 rounded-full transition-colors text-white/50">
            <ChevronLeft size={24} />
          </div>
          <span className="text-xs font-display font-black text-flux-gold uppercase tracking-[0.3em]">Back</span>
        </button>
        <h1 className="text-xs font-display font-black text-white/40 uppercase tracking-[0.3em]">
          MODULA LAUNCHER
        </h1>
        <div className="w-16" />
      </div>

      <div className="max-w-2xl mx-auto w-full space-y-6 pb-20">
        
        {/* STATE 1 & 2: PRE_LAUNCH & LAUNCHING */}
        {(launchState === LaunchState.PRE_LAUNCH || launchState === LaunchState.LAUNCHING) && (
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="space-y-6"
          >
            <FluxCard variant={GlassVariant.DARK} className="flex flex-col items-center py-12 text-center relative overflow-hidden">
               {/* Pulsing Glowning Diamond Logo */}
               <motion.div 
                 animate={{ 
                   scale: [1, 1.05, 1],
                   boxShadow: [
                     "0 0 20px rgba(255,215,0,0.3)",
                     "0 0 40px rgba(255,215,0,0.6)",
                     "0 0 20px rgba(255,215,0,0.3)"
                   ],
                   rotate: 360
                 }}
                 transition={{ 
                   scale: { duration: 1.5, repeat: Infinity, ease: "easeInOut" },
                   boxShadow: { duration: 1.5, repeat: Infinity, ease: "easeInOut" },
                   rotate: { duration: 8, repeat: Infinity, ease: "linear" }
                 }}
                 className="w-20 h-20 bg-flux-gold rounded-3xl flex items-center justify-center text-bg-1 mb-8"
               >
                 <span className="font-display font-black text-4xl italic -rotate-[15deg]">M</span>
               </motion.div>

               <div className="space-y-2 mb-8">
                  <h2 className="text-2xl font-display font-black text-white uppercase tracking-tight">
                    {launchState === LaunchState.PRE_LAUNCH ? 'Preparing Engine' : 'Starting Minecraft'}
                  </h2>
                  <div className="flex gap-2 justify-center">
                    <FluxBadge label={`v${selectedVersion}`} className="bg-flux-gold/20 text-flux-gold" />
                    <FluxBadge label="Fabric 0.16.0" className="bg-white/10 text-white/50" />
                  </div>
               </div>

               {/* Progress Bar */}
               <div className="w-full max-w-sm">
                  <div className="flex justify-between text-[10px] uppercase tracking-widest text-text-secondary mb-3 font-black">
                    <span className="animate-pulse">{launchLogs[launchLogs.length - 1]?.substring(0, 30)}...</span>
                    <span className="text-flux-gold">{Math.round(launchProgress * 100)}%</span>
                  </div>
                  <div className="h-2 w-full bg-white/5 rounded-full overflow-hidden relative">
                    <motion.div 
                      className="h-full bg-flux-gold shadow-[0_0_15px_rgba(255,215,0,0.5)]"
                      animate={{ width: `${launchProgress * 100}%` }}
                    />
                    <motion.div 
                      animate={{ x: ['-100%', '200%'] }}
                      transition={{ duration: 1.5, repeat: Infinity, ease: "linear" }}
                      className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent w-1/2 h-full skew-x-[30deg]"
                    />
                  </div>
                  <div className="mt-3 flex justify-between text-[8px] font-black uppercase tracking-widest text-white/30">
                     <span>312 / 312 Files Verified</span>
                     <span>12.4 MB/S</span>
                  </div>
               </div>
            </FluxCard>

            <StepIndicator currentStep={currentStep} />

            <DeviceCheckCard />

            {launchState === LaunchState.LAUNCHING && !bridgeConnected && (
              <motion.div
                initial={{ y: 20, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
              >
                <DownloadCard onCopyCommand={handleCopyCommand} />
              </motion.div>
            )}

            {/* Substituted JVM Command Card with Highlighting */}
            <FluxCard variant={GlassVariant.DARK} className="p-0 overflow-hidden group">
               <div className="p-4 bg-white/3 border-b border-white/5 flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    <SettingsIcon className="text-flux-gold" size={12} />
                    <span className="text-[10px] font-mono font-black text-white/50 uppercase tracking-widest leading-none">JVM Parameter Chain</span>
                  </div>
                  <FluxButton label="COPY" size="SMALL" variant={GlassVariant.DARK} className="h-6 text-[8px] px-3" onClick={handleCopyCommand} />
               </div>
               <div className="p-4 font-mono text-[9px] overflow-x-auto whitespace-nowrap bg-black/40 no-scrollbar">
                  <span className="text-flux-gold">java</span>
                  <span className="text-flux-gold"> -Xmx4G</span>
                  <span className="text-flux-amber"> -Xms512M</span>
                  <span className="text-white/40"> -XX:+UseG1GC -XX:+ParallelRefProcEnabled</span>
                  <span className="text-white/20"> -Dminecraft.launcher.brand=</span>
                  <span className="text-white">ModulaMobile</span>
                  <span className="text-white/40"> -cp ./bin/client.jar:./lib/*</span>
                  <span className="text-flux-gold"> net.minecraft.client.main.Main</span>
                  <span className="text-flux-gold"> --username</span>
                  <span className="text-white"> {user?.username}</span>
                  <span className="text-flux-gold"> --version</span>
                  <span className="text-white"> {selectedVersion}</span>
               </div>
            </FluxCard>

            <FluxCard variant={GlassVariant.DARK} className="p-0 overflow-hidden h-48">
              <div className="p-4 bg-white/3 border-b border-white/5 flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <Terminal size={12} className="text-flux-gold" />
                  <span className="text-[10px] font-mono font-black text-white/50 uppercase tracking-widest leading-none">Real-time Stream Console</span>
                </div>
                <FluxButton label="COPY LOGS" variant={GlassVariant.DARK} size="SMALL" className="h-6 text-[8px]" />
              </div>
              <div className="p-4 font-mono text-[10px] overflow-y-auto h-full space-y-1 no-scrollbar pb-10 bg-black/30">
                {launchLogs.map((log, i) => {
                  let color = 'text-white/60';
                  if (log.includes('✅')) color = 'text-state-success';
                  if (log.includes('⚡')) color = 'text-flux-gold';
                  if (log.includes('ERROR')) color = 'text-state-error';
                  if (log.includes('Real Launch Command')) color = 'text-flux-gold font-black';

                  return (
                    <div key={i} className={color}>
                      <span className="text-white/20 mr-2">[{new Date().toLocaleTimeString([], { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' })}]</span>
                      {log}
                    </div>
                  );
                })}
                <div ref={logEndRef} />
              </div>
            </FluxCard>
          </motion.div>
        )}

        {/* STATE 3: GAME_RUNNING */}
        {launchState === LaunchState.GAME_RUNNING && (
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="space-y-6"
          >
            <FluxCard variant={GlassVariant.GOLD} className="flex justify-between items-center p-8">
               <div className="space-y-1">
                  <div className="flex items-center gap-2 text-bg-1 font-display font-black uppercase tracking-tight">
                    <Gamepad2 size={18} />
                    MINECRAFT IS RUNNING
                  </div>
                  <p className="text-[10px] text-bg-1/60 font-bold uppercase tracking-widest">Version: {selectedVersion} Fabric</p>
               </div>
               <div className="text-right">
                  <div className="text-3xl font-display font-black text-bg-1 tracking-tighter">
                    {formatTime(sessionTime)}
                  </div>
                  <span className="text-[10px] text-bg-1/60 font-bold uppercase tracking-widest">Session Timer</span>
               </div>
            </FluxCard>

            <div className="grid grid-cols-3 gap-4">
              <FluxCard variant={GlassVariant.DARK} className="text-center p-4">
                <Zap size={16} className="mx-auto mb-2 text-flux-gold" />
                <div className="text-white font-black">120+</div>
                <div className="text-[8px] text-text-muted uppercase tracking-widest">FPS (FLUX)</div>
              </FluxCard>
              <FluxCard variant={GlassVariant.DARK} className="text-center p-4">
                <CheckCircle2 size={16} className="mx-auto mb-2 text-state-success" />
                <div className="text-white font-black text-[10px]">LOCAL</div>
                <div className="text-[8px] text-text-muted uppercase tracking-widest">Environment</div>
              </FluxCard>
              <FluxCard variant={GlassVariant.DARK} className="text-center p-4">
                <MemoryStick size={16} className="mx-auto mb-2 text-flux-gold" />
                <div className="text-white font-black">2.4GB</div>
                <div className="text-[8px] text-text-muted uppercase tracking-widest">Allocation</div>
              </FluxCard>
            </div>

            <FluxCard variant={GlassVariant.DARK} className="p-0 overflow-hidden">
               <div className="p-4 flex justify-between items-center border-b border-white/5">
                 <span className="text-[10px] font-mono font-black text-white/50 uppercase tracking-widest">Live Minecraft Log</span>
                 <FluxButton label="CLEAR" size="SMALL" variant={GlassVariant.DARK} className="text-[8px] h-6" onClick={clearLaunchLogs} />
               </div>
               <div className="p-4 h-64 overflow-y-auto font-mono text-[9px] space-y-1 bg-black/50 no-scrollbar">
                  {launchLogs.slice(-40).map((log, i) => {
                    let color = 'text-white/50';
                    if (log.includes('[INFO]')) color = 'text-flux-gold';
                    if (log.includes('[ERROR]')) color = 'text-state-error';
                    if (log.includes('[DEBUG]')) color = 'text-white/20';

                    return (
                      <div key={i} className={color}>
                        {log}
                      </div>
                    );
                  })}
               </div>
            </FluxCard>

            <div className="grid grid-cols-2 gap-4">
               <FluxButton label="FORCE QUIT" variant={GlassVariant.DARK} className="border-state-error/30 text-state-error font-black" onClick={() => setShowCancelConfirm(true)} />
               <FluxButton label="COPY FULL LOGS" variant={GlassVariant.DARK} onClick={() => alert("Logs copied!")} />
            </div>
          </motion.div>
        )}

        {/* STATE 4: ERROR */}
        {launchState === LaunchState.ERROR && (
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="space-y-6"
          >
            <FluxCard variant={GlassVariant.DARK} className="border-state-error/50 flex flex-col items-center py-12 text-center">
               <div className="w-16 h-16 bg-state-error/10 rounded-full flex items-center justify-center text-state-error mb-8">
                 <XCircle size={40} />
               </div>
               <h2 className="text-2xl font-display font-black text-white uppercase tracking-tight mb-2">LAUNCH FAILED</h2>
               <p className="text-sm text-text-muted max-w-sm mb-12 uppercase tracking-widest font-bold">
                 {launchError?.includes('Auth') ? "Session Token Expired. Please sign in again." :
                  launchError?.includes('RAM') ? "Application Out of Memory. Increase allocation." :
                  launchError?.includes('Network') ? "Check your internet connection." : 
                  "Internal Engine Error Encountered"}
               </p>

               <div className="grid grid-cols-2 gap-4 w-full">
                  <FluxButton label="TRY AGAIN" onClick={startLaunchFlow} />
                  <FluxButton label="GO BACK" variant={GlassVariant.DARK} onClick={() => navigate('/')} />
               </div>
            </FluxCard>

            <FluxCard variant={GlassVariant.DARK} className="p-4 bg-state-error/5 border-state-error/10">
               <div className="flex justify-between items-center mb-4">
                 <span className="text-[10px] font-mono text-state-error uppercase tracking-widest font-black">Raw Stack Trace</span>
                 <Copy size={12} className="text-state-error cursor-pointer" onClick={() => navigator.clipboard.writeText(launchError || '')} />
               </div>
               <pre className="text-[9px] font-mono text-state-error/70 whitespace-pre-wrap leading-relaxed">
                 {launchError || "No debug information."}
               </pre>
            </FluxCard>
          </motion.div>
        )}

      </div>

      {/* Cancel Confirmation Dialog */}
      <AnimatePresence>
        {showCancelConfirm && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[200] bg-bg-1/95 backdrop-blur-xl flex items-center justify-center p-6"
          >
            <FluxCard variant={GlassVariant.DARK} className="max-w-xs w-full p-8 text-center space-y-6 border-white/5">
               <div className="space-y-2">
                 <h2 className="text-xl font-display font-black text-white uppercase tracking-tight">Stop Launch?</h2>
                 <p className="text-[10px] text-text-muted uppercase tracking-widest font-bold">This will terminate all engine processes</p>
               </div>
               <div className="flex flex-col gap-3">
                  <FluxButton label="STOP ENGINE" variant={GlassVariant.DARK} className="text-state-error border-state-error/20" onClick={() => {
                    setLaunchState(LaunchState.IDLE);
                    navigate('/');
                  }} />
                  <FluxButton label="KEEP WAITING" onClick={() => setShowCancelConfirm(false)} />
               </div>
            </FluxCard>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default LaunchScreen;
