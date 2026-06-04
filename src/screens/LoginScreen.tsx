import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { User, Shield, AlertTriangle, Loader2 } from 'lucide-react';
import { 
  FluxCard, 
  GlassVariant, 
  FluxButton, 
  FluxBadge,
  FluxDivider
} from '../components/glass/GlassComponents';
import { useLauncherStore } from '../store/launcherStore';

const LoginScreen: React.FC = () => {
  const { loginMicrosoft, loginOffline } = useLauncherStore();
  const [offlineUsername, setOfflineUsername] = useState('');
  const [isAuthenticating, setIsAuthenticating] = useState(false);
  const [authStep, setAuthStep] = useState('');

  const handleMicrosoftLogin = () => {
    setIsAuthenticating(true);
    setAuthStep('Redirecting to login.microsoftonline.com...');
    
    // Simulate redirection delay
    setTimeout(() => {
      setAuthStep('Awaiting Authorization Callback...');
      setTimeout(() => {
        setAuthStep('Exchanging OAuth Authorization Code...');
        setTimeout(() => {
          setAuthStep('Authenticating with Xbox Live (XBL3.0)...');
          setTimeout(() => {
            setAuthStep('Retrieving Minecraft Identity Profile...');
            setTimeout(() => {
              loginMicrosoft('PixelBuff_Steve', 'vnsuraj2009@gmail.com', '550e8400-e29b-41d4-a716-446655440000');
              setIsAuthenticating(false);
            }, 800);
          }, 800);
        }, 800);
      }, 1500);
    }, 1000);
  };

  const handleOfflineLogin = async () => {
    if (offlineUsername.length >= 3) {
      setIsAuthenticating(true);
      setAuthStep('Generating Secure Player Identity...');
      await loginOffline(offlineUsername);
      setIsAuthenticating(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] bg-bg-1 flex flex-col overflow-y-auto no-scrollbar">
      {/* Top Hero Section */}
      <div className="relative h-[40vh] flex flex-col items-center justify-center p-8">
        <motion.div
          initial={{ scale: 0.5, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.8, ease: "backOut" }}
          className="w-24 h-24 bg-flux-gold/10 rounded-full flex items-center justify-center mb-6 border border-flux-gold/20 shadow-[0_0_50px_-10px_rgba(255,215,0,0.3)]"
        >
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 20, repeat: Infinity, ease: "linear" }}
            className="absolute inset-0 border border-dashed border-flux-gold/30 rounded-full"
          />
          <div className="text-flux-gold text-5xl font-black">M</div>
        </motion.div>
        
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="text-center"
        >
          <h1 className="text-3xl font-display font-black text-white uppercase tracking-tighter">Modula Mobile</h1>
          <p className="text-sm text-text-muted uppercase tracking-[0.3em] mt-2">Next-Gen Minecraft Performance</p>
        </motion.div>
      </div>

      {/* Login Options */}
      <div className="px-6 pb-12 space-y-8 max-w-md mx-auto w-full">
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.4 }}
        >
          <FluxCard variant={GlassVariant.GOLD} className="p-6">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center">
                <Shield className="text-white" size={24} />
              </div>
              <div>
                <h3 className="text-lg font-display font-bold text-white uppercase tracking-tight">Microsoft Account</h3>
                <p className="text-[10px] text-white/70 uppercase tracking-widest">Premium • Online Play</p>
              </div>
            </div>
            <FluxButton 
              label="SIGN IN WITH MICROSOFT" 
              className="w-full h-12 text-sm"
              onClick={handleMicrosoftLogin}
              disabled={isAuthenticating}
            />
          </FluxCard>
        </motion.div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          className="flex items-center gap-4 px-4"
        >
          <FluxDivider className="flex-1" />
          <span className="text-[10px] text-text-muted font-bold uppercase tracking-widest">OR</span>
          <FluxDivider className="flex-1" />
        </motion.div>

        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.6 }}
        >
          <FluxCard variant={GlassVariant.DARK} className="p-6">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-12 h-12 bg-bg-4 rounded-xl flex items-center justify-center">
                <User className="text-flux-gold" size={24} />
              </div>
              <div>
                <h3 className="text-lg font-display font-bold text-white uppercase tracking-tight">Offline Account</h3>
                <p className="text-[10px] text-text-muted uppercase tracking-widest">Singleplayer • Custom Servers</p>
              </div>
            </div>
            
            <div className="space-y-4">
              <div>
                <label className="text-[10px] text-text-muted font-bold uppercase tracking-[0.2em] mb-2 block">Username</label>
                <input 
                  type="text" 
                  placeholder="Enter your name (3-16 chars)"
                  className="w-full glass-dark rounded-xl py-3 px-4 text-sm text-white focus:outline-none focus:ring-1 focus:ring-flux-gold"
                  value={offlineUsername}
                  onChange={(e) => setOfflineUsername(e.target.value)}
                />
              </div>
              <FluxButton 
                label="PLAY OFFLINE" 
                variant="GHOST" 
                className="w-full h-12 text-sm"
                disabled={offlineUsername.length < 3 || isAuthenticating}
                onClick={handleOfflineLogin}
              />
            </div>
          </FluxCard>
        </motion.div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8 }}
          className="text-center space-y-2"
        >
          <p className="text-[10px] text-state-error uppercase tracking-widest flex items-center justify-center gap-2">
            <AlertTriangle size={10} />
            Offline mode cannot access official servers
          </p>
          <button className="text-[10px] text-flux-gold hover:underline uppercase tracking-widest font-bold">
            About ModulaMC
          </button>
        </motion.div>
      </div>

      {/* Auth Loading Overlay */}
      <AnimatePresence>
        {isAuthenticating && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[100] bg-bg-1/90 backdrop-blur-md flex flex-col items-center justify-center p-8"
          >
            <div className="w-20 h-20 relative mb-8">
               <motion.div 
                animate={{ rotate: 360 }}
                transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                className="absolute inset-0 border-2 border-t-flux-gold border-r-transparent border-b-transparent border-l-transparent rounded-full"
               />
               <div className="absolute inset-4 bg-flux-gold/10 rounded-full flex items-center justify-center">
                  <Shield size={32} className="text-flux-gold animate-pulse" />
               </div>
            </div>
            <h2 className="text-xl font-display font-black text-white uppercase tracking-tight mb-2">Authenticating</h2>
            <p className="text-sm text-flux-gold animate-pulse font-mono">{authStep}</p>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default LoginScreen;
