import React, { useState, useEffect } from 'react';
import { motion, useAnimationFrame } from 'motion/react';
import { FluxProgressBar } from './GlassComponents';

interface SkinViewerProps {
  username?: string;
  customUrl?: string | null;
  animation?: 'standing' | 'walking' | 'running';
  className?: string;
}

export const SkinViewer: React.FC<SkinViewerProps> = ({ 
  username = "Steve", 
  customUrl = null,
  animation = 'standing',
  className = "" 
}) => {
  const [loading, setLoading] = useState(true);
  const [rotation, setRotation] = useState(0);
  const [yOffset, setYOffset] = useState(0);
  const [error, setError] = useState(false);

  // Sync animation parameters based on state
  const animConfig = {
    standing: { speed: 1200, sway: 8, bounce: 2, bounceFreq: 4 },
    walking: { speed: 300, sway: 12, bounce: 6, bounceFreq: 2 },
    running: { speed: 150, sway: 18, bounce: 10, bounceFreq: 1 }
  }[animation];

  useAnimationFrame((time) => {
    // Sway (Side to side)
    const sway = Math.sin(time / animConfig.speed) * animConfig.sway;
    setRotation(sway);
    
    // Bobbing (Up and down should be a gentle offset, not a jump)
    // Removed Math.abs to make it a continuous smooth wave
    const bob = Math.sin(time / (animConfig.speed / 2)) * animConfig.bounce;
    setYOffset(bob);
  });

  const skinUrl = customUrl || (error 
    ? `https://mc-heads.net/body/Steve/280` 
    : `https://mc-heads.net/body/${username}/280`);

  return (
    <div className={`relative flex flex-col items-center justify-center bg-bg-2 border border-white/10 rounded-xl overflow-hidden ${className}`}>
      <div className="absolute top-4 left-6">
        <div className="flex items-center gap-2">
          <div className="w-1.5 h-1.5 rounded-full bg-flux-gold animate-pulse" />
          <span className="text-[9px] font-mono font-bold text-flux-gold tracking-[0.4em] uppercase opacity-50">Flux Engine Rendering</span>
        </div>
      </div>

      {loading && (
        <div className="absolute inset-0 flex flex-col items-center justify-center p-12 z-20 bg-bg-2">
           <FluxProgressBar progress={0.5} label="Streaming Textures..." indeterminate />
        </div>
      )}

      <motion.div
        animate={{ 
           rotateY: rotation,
           scale: loading ? 0.9 : 1,
           opacity: loading ? 0 : 1,
           y: -yOffset
        }}
        transition={{ 
          rotateY: { duration: 0 },
          y: { duration: 0 },
          scale: { duration: 0.5 },
          opacity: { duration: 0.5 }
        }}
        className="w-full h-full flex items-center justify-center perspective-[1000px] py-12"
      >
        <img 
          key={skinUrl}
          src={skinUrl}
          alt="Minecraft Skin"
          className="h-full object-contain filter drop-shadow-[0_25px_40px_rgba(0,0,0,0.6)]"
          onLoad={() => setLoading(false)}
          onError={() => {
            if (!customUrl) setError(true);
            setLoading(false);
          }}
          referrerPolicy="no-referrer"
        />
      </motion.div>
      
      {/* Dynamic Ambient Ground Shadow */}
      <motion.div 
        animate={{ 
          scaleX: 1 + (yOffset / 100), 
          opacity: 0.4 - (yOffset / 100) 
        }}
        transition={{ duration: 0 }}
        className="absolute bottom-12 w-24 h-5 bg-black/50 blur-xl rounded-[100%]" 
      />

      <div className="absolute bottom-4 right-6 flex items-center gap-2">
        <div className="text-[8px] font-mono text-flux-gold uppercase animate-pulse">{animation}</div>
        <div className="w-1 h-1 rounded-full bg-white/20" />
        <span className="text-[8px] font-mono text-text-muted uppercase tracking-widest">{username}</span>
      </div>
    </div>
  );
};
