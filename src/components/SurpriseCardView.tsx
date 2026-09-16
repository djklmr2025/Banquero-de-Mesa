import React, { useRef } from 'react';
import { Sparkles, Upload, Trash2, ShieldAlert, Award, DollarSign } from 'lucide-react';
import { SurpriseCard } from '../types/game';
import { soundFx } from '../services/soundService';

interface SurpriseCardViewProps {
  card: SurpriseCard;
  isEditable?: boolean;
  onUpdate?: (updated: SurpriseCard) => void;
  onDelete?: () => void;
  onClose?: () => void;
}

const BORDER_COLORS = [
  '#F59E0B', '#E11D48', '#8B5CF6', '#10B981', 
  '#38BDF8', '#F97316', '#FFFFFF', '#64748B'
];

export const SurpriseCardView: React.FC<SurpriseCardViewProps> = ({
  card,
  isEditable = false,
  onUpdate,
  onDelete,
  onClose
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      onUpdate?.({
        ...card,
        backgroundImage: dataUrl
      });
      soundFx.playCoin();
    };
    reader.readAsDataURL(file);
  };

  const handleRemoveImage = () => {
    onUpdate?.({
      ...card,
      backgroundImage: undefined
    });
  };

  const borderColor = card.borderColor || '#F59E0B';

  return (
    <div 
      className="w-full max-w-[280px] sm:max-w-[300px] aspect-[5/7] rounded-3xl p-5 shadow-2xl relative overflow-hidden flex flex-col justify-between transition-all hover:scale-[1.02] border-4 select-none"
      style={{ 
        borderColor: borderColor,
        boxShadow: `0 20px 40px -15px ${borderColor}40`
      }}
    >
      {/* Background Image or Gradient */}
      {card.backgroundImage ? (
        <>
          <img
            src={card.backgroundImage}
            alt={card.title}
            className="absolute inset-0 w-full h-full object-cover"
          />
          {/* Dark scrim for readability */}
          <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/70 to-slate-900/60 pointer-events-none"></div>
        </>
      ) : (
        <div className="absolute inset-0 bg-gradient-to-br from-slate-900 via-slate-950 to-slate-900 pointer-events-none">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(245,158,11,0.15)_0%,transparent_60%)]"></div>
        </div>
      )}

      {/* Top Header: Badge & Icon */}
      <div className="relative z-10 flex items-center justify-between gap-2">
        <span 
          className="text-[10px] font-black uppercase tracking-widest px-2.5 py-1 rounded-full border shadow-sm backdrop-blur-md"
          style={{ 
            backgroundColor: `${borderColor}25`, 
            color: borderColor,
            borderColor: `${borderColor}60`
          }}
        >
          {card.type.includes('PAY') ? '⚠️ Trampa / Multa' : card.type.includes('RECEIVE') ? '⭐ Suerte / Premio' : '⚡ Destino'}
        </span>

        <span className="text-[10px] font-mono text-slate-400 capitalize">
          {card.rarity || 'común'}
        </span>
      </div>

      {/* Center: Title & Description */}
      <div className="relative z-10 my-auto flex flex-col gap-2 py-2">
        {isEditable ? (
          <input
            type="text"
            value={card.title}
            onChange={e => onUpdate?.({ ...card, title: e.target.value })}
            placeholder="Título de la Carta"
            className="w-full bg-black/50 border border-slate-700 rounded-lg px-2.5 py-1 text-sm font-black text-white focus:outline-none focus:border-amber-400"
          />
        ) : (
          <h3 className="text-lg font-black text-white leading-tight drop-shadow-md">
            {card.title}
          </h3>
        )}

        {isEditable ? (
          <textarea
            rows={3}
            value={card.description}
            onChange={e => onUpdate?.({ ...card, description: e.target.value })}
            placeholder="Escribe la regla o consecuencia de esta carta..."
            className="w-full bg-black/50 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-amber-400 resize-none"
          />
        ) : (
          <p className="text-xs text-slate-300 leading-relaxed drop-shadow">
            {card.description}
          </p>
        )}

        {card.amount && (
          <div className="mt-1 flex items-center gap-1.5">
            <span className="text-[10px] uppercase font-bold text-slate-400">Impacto:</span>
            <span className={`font-mono font-black text-sm ${card.type.includes('RECEIVE') ? 'text-emerald-400' : 'text-rose-400'}`}>
              {card.type.includes('RECEIVE') ? '+' : '-'}${card.amount.toLocaleString()}
            </span>
          </div>
        )}
      </div>

      {/* Footer: Controls & Edit Pickers */}
      <div className="relative z-10 pt-2 border-t border-white/10 flex flex-col gap-2">
        {isEditable ? (
          <>
            {/* Border Color Swatches */}
            <div className="flex items-center justify-between">
              <span className="text-[9px] font-bold text-slate-400 uppercase">Contorno:</span>
              <div className="flex items-center gap-1">
                {BORDER_COLORS.map(c => (
                  <button
                    key={c}
                    type="button"
                    onClick={() => onUpdate?.({ ...card, borderColor: c })}
                    className={`w-3.5 h-3.5 rounded-full transition-transform cursor-pointer ${
                      card.borderColor === c ? 'scale-125 ring-2 ring-white' : 'opacity-70 hover:opacity-100'
                    }`}
                    style={{ backgroundColor: c }}
                  />
                ))}
              </div>
            </div>

            {/* Background Image Upload Button */}
            <div className="flex items-center justify-between gap-2 pt-1">
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleImageUpload}
                className="hidden"
              />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="flex-1 py-1 px-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-[10px] font-bold flex items-center justify-center gap-1 transition-all cursor-pointer"
              >
                <Upload className="w-3 h-3 text-amber-400" />
                <span>{card.backgroundImage ? 'Cambiar Fondo' : 'Subir Fondo'}</span>
              </button>

              {card.backgroundImage && (
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="p-1 bg-rose-950/60 hover:bg-rose-900 text-rose-300 rounded-lg text-[10px] cursor-pointer"
                  title="Quitar fondo"
                >
                  <Trash2 className="w-3 h-3" />
                </button>
              )}

              {onDelete && (
                <button
                  type="button"
                  onClick={onDelete}
                  className="p-1 text-slate-500 hover:text-rose-400 transition-colors cursor-pointer"
                  title="Eliminar carta"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              )}
            </div>
          </>
        ) : (
          <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono">
            <span>FOTORAMA OFICIAL</span>
            <span>ID: {card.id}</span>
          </div>
        )}
      </div>
    </div>
  );
};
