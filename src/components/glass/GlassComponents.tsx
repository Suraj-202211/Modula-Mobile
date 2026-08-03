import React, { useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { useLauncherStore } from '../../store/launcherStore';

export enum GlassVariant {
  ULTRA_DARK = 'glass-ultra-dark',
  DARK = 'glass-dark',
  NEUTRAL = 'glass-neutral',
  GOLD = 'glass-gold',
  HERO = 'glass-hero'
}

interface FluxCardProps {
  variant?: GlassVariant;
  radius?: string; // e.g. '16px', 'lg', etc
  children: React.ReactNode;
  className?: string;
  onClick?: () => void;
}

export const FluxCard: React.FC<FluxCardProps> = ({
  variant = GlassVariant.DARK,
  radius = 'rounded-md', // corresponds to 16dp
  children,
  className = '',
  onClick
}) => {
  const { settings } = useLauncherStore();
  const showShadows = settings.dynamicShadows && !settings.performanceMode;
  
  return (
    <motion.div
      whileHover={onClick ? { scale: 1.01 } : {}}
      whileTap={onClick ? { scale: 0.99 } : {}}
      transition={{ type: 'spring', stiffness: 500, damping: 30 }}
      onClick={onClick}
      className={`
        ${variant}
        ${radius}
        p-4
        ${onClick ? 'cursor-pointer' : ''}
        ${showShadows ? 'shadow-lg shadow-black/20' : ''}
        ${className}
      `}
      style={{
        backdropFilter: settings.performanceMode ? 'none' : `blur(${16 * (settings.transparencyLevel / 100)}px)`,
      }}
    >
      {children}
    </motion.div>
  );
};

export const FluxButton: React.FC<{
  label: string;
  onClick?: () => void;
  variant?: 'PRIMARY' | 'GHOST' | 'DANGER';
  size?: 'SMALL' | 'MEDIUM' | 'LARGE';
  className?: string;
  icon?: React.ReactNode;
}> = ({ label, onClick, variant = 'PRIMARY', size = 'MEDIUM', className = '', icon }) => {
  const { settings } = useLauncherStore();
  
  const styles = {
    PRIMARY: `bg-flux-gold text-text-on-gold font-bold ${settings.bloomEffect ? 'shadow-[0_0_15px_-3px_rgba(255,215,0,0.5)]' : ''}`,
    GHOST: 'border border-flux-gold text-flux-gold hover:bg-flux-gold/10',
    DANGER: `bg-state-error/20 border border-state-error text-state-error hover:bg-state-error/30 ${settings.bloomEffect ? 'shadow-[0_0_10px_rgba(239,68,68,0.2)]' : ''}`
  };

  const sizes = {
    SMALL: 'px-3 py-1.5 text-[10px]',
    MEDIUM: 'px-6 py-3 text-sm',
    LARGE: 'px-8 py-4 text-base'
  };

  return (
    <motion.button
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      transition={{ type: 'spring', stiffness: 500, damping: 30 }}
      onClick={onClick}
      className={`
        ${styles[variant]}
        ${sizes[size]}
        rounded-pill transition-all duration-200
        flex items-center justify-center gap-2
        font-display tracking-wider
        ${className}
      `}
    >
      {icon}
      {label}
    </motion.button>
  );
};

export const FluxHeroCard: React.FC<{
  backgroundImage?: string;
  children: React.ReactNode;
  className?: string;
}> = ({ backgroundImage, children, className = '' }) => {
  return (
    <div className={`relative overflow-hidden glass-hero rounded-xl min-h-[220px] ${className}`}>
      {backgroundImage && (
        <div
          className="absolute inset-0 z-0 bg-cover bg-center brightness-50 opacity-40 blur-[2px]"
          style={{ backgroundImage: `url(${backgroundImage})` }}
        />
      )}
      <div className="relative z-10 p-6 h-full flex flex-col justify-between">
        {children}
      </div>
    </div>
  );
};

export const FluxBadge: React.FC<{
  label: string;
  color?: string;
  className?: string;
}> = ({ label, color = 'bg-flux-gold/20 text-flux-gold border-flux-gold/40', className = '' }) => {
  return (
    <span className={`px-2 py-0.5 rounded-xs text-[10px] font-bold uppercase tracking-widest border ${color} ${className}`}>
      {label}
    </span>
  );
};

export const FluxDivider: React.FC<{ withGlow?: boolean }> = ({ withGlow = false }) => {
  return (
    <div className={`h-px w-full my-4 bg-white/10 ${withGlow ? 'shadow-[0_0_10px_rgba(255,215,0,0.3)] bg-flux-gold/30' : ''}`} />
  );
};

export const FluxListTile: React.FC<{
  leading?: React.ReactNode;
  title: string;
  subtitle?: string;
  trailing?: React.ReactNode;
  onClick?: () => void;
  className?: string;
}> = ({ leading, title, subtitle, trailing, onClick, className = '' }) => {
  return (
    <FluxCard
      onClick={onClick}
      className={`flex items-center gap-4 py-3 px-4 ${className}`}
      variant={GlassVariant.DARK}
    >
      {leading && <div className="flex-shrink-0">{leading}</div>}
      <div className="flex-grow min-w-0">
        <h3 className="text-md font-medium text-white truncate">{title}</h3>
        {subtitle && <p className="text-xs text-text-secondary truncate">{subtitle}</p>}
      </div>
      {trailing && <div className="flex-shrink-0">{trailing}</div>}
    </FluxCard>
  );
};

export const FluxChip: React.FC<{
  label: string;
  selected?: boolean;
  icon?: React.ReactNode;
  onClick?: () => void;
  className?: string;
}> = ({ label, selected, icon, onClick, className = '' }) => {
  return (
    <button
      onClick={onClick}
      className={`
        px-3 py-1 rounded-pill text-xs flex items-center gap-1.5 transition-all
        ${selected ? 'bg-flux-gold text-text-on-gold font-bold' : 'glass-neutral text-text-secondary hover:text-white'}
        ${className}
      `}
    >
      {icon}
      {label}
    </button>
  );
};

export const FluxIconButton: React.FC<{
  icon: React.ReactNode;
  onClick?: () => void;
  size?: 'SMALL' | 'MEDIUM' | 'LARGE';
  className?: string;
}> = ({ icon, onClick, size = 'MEDIUM', className = '' }) => {
  const sizes = {
    SMALL: 'p-1.5',
    MEDIUM: 'p-3',
    LARGE: 'p-5'
  };

  return (
    <motion.button
      whileHover={{ scale: 1.05, backgroundColor: 'rgba(255, 215, 0, 0.1)' }}
      whileTap={{ scale: 0.95 }}
      transition={{ type: 'spring', stiffness: 500, damping: 30 }}
      onClick={onClick}
      className={`glass-neutral rounded-full text-flux-gold ${sizes[size]} ${className}`}
    >
      {icon}
    </motion.button>
  );
};

export const FluxSectionHeader: React.FC<{
  title: string;
  action?: React.ReactNode;
  className?: string;
}> = ({ title, action, className = '' }) => {
  return (
    <div className={`flex items-center justify-between mt-8 mb-4 ${className}`}>
      <h2 className="text-sm font-display font-bold tracking-[0.1em] text-flux-gold uppercase">{title}</h2>
      {action}
    </div>
  );
};

export const FluxTopBar: React.FC<{
  title: string;
  navigationIcon?: React.ReactNode;
  actions?: React.ReactNode;
  isScrolled?: boolean;
}> = ({ title, navigationIcon, actions, isScrolled }) => {
  return (
    <header className={`sticky top-0 z-50 transition-all duration-300 px-4 h-16 flex items-center justify-between ${isScrolled ? 'glass-dark h-14' : 'bg-transparent'}`}>
      <div className="flex items-center gap-3">
        {navigationIcon}
        <h1 className="text-xl font-display font-bold tracking-tight text-white uppercase">{title}</h1>
      </div>
      <div className="flex items-center gap-2">
        {actions}
      </div>
    </header>
  );
};

const getIconForId = (id: string) => {
  const size = 20;
  switch (id) {
    case '/':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-home">
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
      );
    case '/versions':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-layers">
          <path d="m12 3-10 5 10 5 10-5-10-5Z" />
          <path d="m2 17 10 5 10-5" />
          <path d="m2 12 10 5 10-5" />
        </svg>
      );
    case '/mods':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-puzzle">
          <path d="M12 22a2 2 0 0 1-2-2v-1a2 2 0 0 0-4 0v1a2 2 0 0 1-2 2H3a1 1 0 0 1-1-1v-1a2 2 0 0 1 2-2h1a2 2 0 0 0 0-4H4a2 2 0 0 1-2-2V7a1 1 0 0 1 1-1h1a2 2 0 0 0 2 0V5a2 2 0 0 1 2-2h3a1 1 0 0 1 1 1v1a2 2 0 0 0 4 0V5a2 2 0 0 1 2-2h1a1 1 0 0 1 1 1v1a2 2 0 0 1-2 2h-1a2 2 0 0 0 0 4h1a2 2 0 0 1 2 2v3a1 1 0 0 1-1 1h-1a2 2 0 0 0-2 2v1a2 2 0 0 1-2 2h-3Z" />
        </svg>
      );
    case '/modpacks':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-package">
          <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
          <polyline points="3.29 7 12 12 20.71 7" />
          <line x1="12" y1="12" x2="12" y2="22" />
        </svg>
      );
    case '/settings':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-settings">
          <circle cx="12" cy="12" r="3" />
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
        </svg>
      );
    case '/profile':
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-user">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      );
    default:
      return null;
  }
};

export const FluxBottomNav: React.FC<{
  items: { icon?: React.ReactNode; label: string; id: string }[];
  selectedId: string;
  onSelect: (id: string) => void;
}> = ({ items, selectedId, onSelect }) => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 glass-dark border-t border-white/5 h-20 px-2 flex items-center justify-around z-50">
      {items.map((item) => {
        const isSelected = selectedId === item.id;
        return (
          <button
            key={item.id}
            onClick={() => onSelect(item.id)}
            className={`flex flex-col items-center gap-1 min-w-[64px] transition-all duration-300 font-sans ${isSelected ? 'text-flux-gold' : 'text-text-muted hover:text-text-secondary'}`}
          >
            <motion.div
              animate={isSelected ? { scale: 1.1, y: -2 } : { scale: 1, y: 0 }}
              transition={{ type: 'spring', stiffness: 400, damping: 25 }}
              className="relative flex items-center justify-center font-sans"
              style={{ fontFamily: 'sans-serif' }}
            >
              {getIconForId(item.id) || item.icon}
              {isSelected && (
                <motion.div
                  layoutId="nav-indicator"
                  className="absolute -bottom-2 left-1/2 -translate-x-1/2 w-1 h-1 bg-flux-gold rounded-full shadow-[0_0_8px_rgba(255,215,0,0.8)]"
                />
              )}
            </motion.div>
            <span className="text-[10px] uppercase font-medium tracking-widest">{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};

export const FluxSlider: React.FC<{
  value: number;
  min?: number;
  max?: number;
  step?: number;
  label: string;
  valueLabel?: string;
  onChange: (val: number) => void;
}> = ({ value, min = 0, max = 100, step = 1, label, valueLabel, onChange }) => {
  return (
    <div className="w-full py-4">
      <div className="flex justify-between items-center mb-4">
        <label className="text-sm font-medium text-text-primary">{label}</label>
        <span className="text-xs font-mono text-flux-gold bg-flux-gold/10 px-2 py-0.5 rounded border border-flux-gold/20">
          {valueLabel || value}
        </span>
      </div>
      <div className="relative h-6 flex items-center">
        <div className="absolute w-full h-1 bg-white/10 rounded-full" />
        <div
          className="absolute h-1 bg-flux-gold rounded-full"
          style={{ width: `${((value - min) / (max - min)) * 100}%` }}
        />
        <input
          type="range"
          min={min}
          max={max}
          step={step}
          value={value}
          onChange={(e) => onChange(Number(e.target.value))}
          className="absolute w-full opacity-0 cursor-pointer h-6"
        />
        <div
          className="absolute w-4 h-4 bg-flux-gold rounded-full border-2 border-white pointer-events-none shadow-[0_0_10px_rgba(255,215,0,0.5)]"
          style={{ left: `calc(${((value - min) / (max - min)) * 100}% - 8px)` }}
        />
      </div>
    </div>
  );
};

export const FluxToggle: React.FC<{
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  label: string;
  subtitle?: string;
}> = ({ checked, onCheckedChange, label, subtitle }) => {
  return (
    <div className="flex items-center justify-between py-3">
      <div className="flex-grow pr-4">
        <h4 className="text-sm font-medium text-white">{label}</h4>
        {subtitle && <p className="text-xs text-text-muted mt-0.5">{subtitle}</p>}
      </div>
      <button
        onClick={() => onCheckedChange(!checked)}
        className={`w-10 h-5 rounded-full transition-all duration-200 relative p-0.5 ${checked ? 'bg-flux-gold shadow-[0_0_10px_rgba(255,215,0,0.4)]' : 'bg-white/10'}`}
      >
        <motion.div
          animate={{ x: checked ? 20 : 0 }}
          transition={{ type: 'spring', stiffness: 600, damping: 30 }}
          className={`w-4 h-4 rounded-full shadow-sm ${checked ? 'bg-text-on-gold' : 'bg-text-secondary'}`}
        />
      </button>
    </div>
  );
};

export const FluxProgressBar: React.FC<{
  progress: number; // 0 to 1
  label?: string;
  className?: string;
  indeterminate?: boolean;
}> = ({ progress, label, className = '', indeterminate }) => {
  return (
    <div className={`w-full ${className}`}>
      {label && <div className="flex justify-between text-[10px] uppercase tracking-widest text-text-secondary mb-1">
        <span>{label}</span>
        {!indeterminate && <span>{Math.round(progress * 100)}%</span>}
      </div>}
      <div className="h-1.5 w-full bg-white/10 rounded-full overflow-hidden relative">
        <motion.div
          initial={indeterminate ? { x: "-100%" } : { width: 0 }}
          animate={indeterminate ? { x: "200%" } : { width: `${progress * 100}%` }}
          transition={indeterminate ? { 
            repeat: Infinity, 
            duration: 1.5, 
            ease: "easeInOut" 
          } : {}}
          className="h-full bg-flux-gold shadow-[0_0_8px_rgba(255,215,0,0.5)] absolute top-0 left-0"
          style={indeterminate ? { width: '40%' } : {}}
        />
      </div>
    </div>
  );
};

export const FluxLoadingOverlay: React.FC<{ message?: string }> = ({ message }) => {
  return (
    <div className="fixed inset-0 z-[100] glass-ultra-dark flex flex-col items-center justify-center gap-6">
      <div className="relative w-20 h-20">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
          className="absolute inset-0 rounded-full border-2 border-flux-gold/20 border-t-flux-gold shadow-[0_0_15px_rgba(255,215,0,0.3)]"
        />
        <motion.div
          animate={{ scale: [0.8, 1.2, 0.8] }}
          transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute inset-0 flex items-center justify-center"
        >
          <div className="w-8 h-8 bg-flux-gold rounded shadow-[0_0_20px_rgba(255,215,0,0.8)]" />
        </motion.div>
      </div>
      {message && (
        <motion.p
          animate={{ opacity: [0.5, 1, 0.5] }}
          transition={{ duration: 1.5, repeat: Infinity }}
          className="text-xs uppercase tracking-[0.2em] font-display text-flux-gold"
        >
          {message}
        </motion.p>
      )}
    </div>
  );
};

export const FluxModal: React.FC<{
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
  maxWidth?: string;
}> = ({ isOpen, onClose, title, children, actions, maxWidth = 'max-w-md' }) => {
  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
          />
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className={`relative w-full ${maxWidth} glass-dark border border-white/10 rounded-xl overflow-hidden shadow-2xl`}
          >
            <div className="p-6">
              <h2 className="text-lg font-display font-bold text-white mb-4 tracking-wide">{title}</h2>
              <div className="mb-6 text-sm text-text-primary">
                {children}
              </div>
              {actions && (
                <div className="flex justify-end gap-3 mt-6">
                  {actions}
                </div>
              )}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export const FluxInput: React.FC<{
  value: string;
  onChange: (val: string) => void;
  placeholder?: string;
  autoFocus?: boolean;
  onEnter?: () => void;
  onEscape?: () => void;
  className?: string;
}> = ({ value, onChange, placeholder, autoFocus, onEnter, onEscape, className = '' }) => {
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (autoFocus && inputRef.current) {
      // Small delay ensures it focuses after modal animation
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }, [autoFocus]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && onEnter) {
      onEnter();
    } else if (e.key === 'Escape' && onEscape) {
      onEscape();
    }
  };

  return (
    <input
      ref={inputRef}
      type="text"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onKeyDown={handleKeyDown}
      placeholder={placeholder}
      className={`w-full bg-black/40 border border-white/10 focus:border-flux-gold/50 rounded-lg px-4 py-3 text-sm text-white placeholder-white/30 outline-none transition-colors ${className}`}
    />
  );
};
