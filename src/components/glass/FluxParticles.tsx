import React, { useEffect, useRef } from 'react';
import { useLauncherStore } from '../../store/launcherStore';

interface Particle {
  x: number;
  y: number;
  speed: number;
  size: number;
  sineFreq: number;
  sineAmp: number;
  color: string;
  opacity: number;
  phase: number;
}

export const FluxParticles: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const { settings } = useLauncherStore();
  const intensity = settings?.particleIntensity ?? 60;
  const isPerformanceMode = settings?.performanceMode ?? false;

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d', { alpha: true });
    if (!ctx) return;

    let animationFrameId: number;
    let particles: Particle[] = [];
    
    const isLowRamMode = settings?.lowRamMode ?? false;
    
    // Performance Mode: reduce particles by 80%, Low RAM: reduce by 95%
    const ramMultiplier = isLowRamMode ? 0.05 : (isPerformanceMode ? 0.2 : 1);
    const effectiveIntensity = intensity * ramMultiplier;
    const particleCount = Math.max(isLowRamMode ? 5 : 0, Math.floor((effectiveIntensity / 100) * 100));

    const THEME_COLORS = {
      DEFAULT: ['#ffd700', '#ff6b00'],
      ONYX: ['#ffffff', '#a1a1aa'],
      VOLCANIC: ['#ef4444', '#f97316'],
      NEON: ['#22d3ee', '#a855f7'],
      ARCTIC: ['#60a5fa', '#818cf8']
    };

    const currentColors = THEME_COLORS[settings.theme] || THEME_COLORS.DEFAULT;

    const createParticles = () => {
      particles = [];
      if (effectiveIntensity <= 0) return;
      
      for (let i = 0; i < particleCount; i++) {
        particles.push({
          x: Math.random(),
          y: Math.random(),
          speed: 0.0002 + Math.random() * 0.0006,
          size: 1.5 + Math.random() * 3,
          sineFreq: 0.5 + Math.random() * 1.5,
          sineAmp: 0.01 + Math.random() * 0.02,
          color: currentColors[Math.floor(Math.random() * currentColors.length)],
          opacity: 0.1 + Math.random() * 0.4,
          phase: Math.random() * Math.PI * 2
        });
      }
    };

    const resize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
      createParticles();
    };

    window.addEventListener('resize', resize);
    resize();

    const draw = (time: number) => {
      if (effectiveIntensity <= 0) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        return;
      }

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // Optimization: No shadow blur in performance mode, and simpler rendering path
      if (!isPerformanceMode) {
        ctx.shadowBlur = 10;
        ctx.globalCompositeOperation = 'source-over';
      } else {
        ctx.shadowBlur = 0;
        ctx.globalCompositeOperation = 'lighter'; // Lighter is often faster on mobile
      }

      // Pre-calculate common values for speed
      const w = canvas.width;
      const h = canvas.height;
      
      // Group particles by color to minimize fillStyle changes
      const groups: Record<string, Particle[]> = {};
      for (let i = 0; i < particles.length; i++) {
        const p = particles[i];
        if (!groups[p.color]) groups[p.color] = [];
        groups[p.color].push(p);
      }

      for (const color in groups) {
        ctx.fillStyle = color;
        if (!isPerformanceMode) ctx.shadowColor = color;
        
        ctx.beginPath();
        const pList = groups[color];
        for (let i = 0; i < pList.length; i++) {
          const p = pList[i];
          p.y -= p.speed;
          if (p.y < -0.1) p.y = 1.1;

          const dx = Math.sin(p.phase + time * 0.001 * p.sineFreq) * p.sineAmp;
          const xPos = Math.round((p.x + dx) * w);
          const yPos = Math.round(p.y * h);

          const fade = p.y < 0.2 ? p.y * 5 : 1;
          ctx.globalAlpha = p.opacity * fade;
          
          // Move to the particle position and draw arc
          ctx.moveTo(xPos + p.size, yPos);
          ctx.arc(xPos, yPos, p.size, 0, 6.28);
        }
        ctx.fill();
      }

      animationFrameId = requestAnimationFrame(draw);
    };

    animationFrameId = requestAnimationFrame(draw);

    return () => {
      window.removeEventListener('resize', resize);
      cancelAnimationFrame(animationFrameId);
    };
  }, [intensity, isPerformanceMode, settings.theme, settings.lowRamMode]);

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 pointer-events-none z-0 opacity-50"
    />
  );
};
