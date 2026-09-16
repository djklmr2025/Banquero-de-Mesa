import React, { useRef } from 'react';
import { Building, Upload, Trash2, Home, Sparkles, DollarSign, Image as ImageIcon } from 'lucide-react';
import { BoardProperty } from '../types/game';
import { soundFx } from '../services/soundService';

interface VerticalPropertyCardProps {
  property: BoardProperty;
  isEditable?: boolean;
  onUpdate?: (updated: BoardProperty) => void;
  onDelete?: () => void;
  onBuy?: () => void;
  canBuy?: boolean;
  ownerName?: string;
  showBuyButton?: boolean;
}

export const VerticalPropertyCard: React.FC<VerticalPropertyCardProps> = ({
  property,
  isEditable = false,
  onUpdate,
  onDelete,
  onBuy,
  canBuy = false,
  ownerName,
  showBuyButton = false
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Upload vertical card photo
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      onUpdate?.({
        ...property,
        image: dataUrl
      });
      soundFx.playCoin();
    };
    reader.readAsDataURL(file);
  };

  // Remove photo
  const handleRemoveImage = () => {
    onUpdate?.({
      ...property,
      image: undefined
    });
  };

  return (
    <div className="w-full max-w-[260px] bg-slate-950 border-2 border-slate-700 rounded-2xl overflow-hidden shadow-2xl flex flex-col justify-between transition-all hover:border-amber-400/80">
      {/* Top Colored Group Header */}
      <div 
        className="p-3 text-center text-white font-black uppercase tracking-wider relative shadow-md"
        style={{ backgroundColor: property.color }}
      >
        <span className="text-[9px] tracking-widest block opacity-80">TÍTULO DE PROPIEDAD</span>
        {isEditable ? (
          <input
            type="text"
            value={property.name}
            onChange={e => onUpdate?.({ ...property, name: e.target.value })}
            className="w-full bg-black/30 border border-white/40 rounded px-1.5 py-0.5 text-xs font-black text-center text-white focus:outline-none"
            placeholder="Nombre de la propiedad"
          />
        ) : (
          <h4 className="text-sm font-black truncate drop-shadow">{property.name}</h4>
        )}
      </div>

      {/* Vertical Image Slot */}
      <div className="relative aspect-[4/3] bg-slate-900 border-y border-slate-800 flex items-center justify-center overflow-hidden group">
        {property.image ? (
          <>
            <img
              src={property.image}
              alt={property.name}
              className="w-full h-full object-cover"
            />
            {isEditable && (
              <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 flex items-center justify-center gap-2 transition-opacity">
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="p-1.5 bg-amber-500 text-slate-950 rounded-lg text-xs font-bold"
                  title="Cambiar foto vertical"
                >
                  <Upload className="w-3.5 h-3.5" />
                </button>
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="p-1.5 bg-rose-600 text-white rounded-lg text-xs font-bold"
                  title="Quitar foto"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="flex flex-col items-center justify-center text-slate-600 p-4 text-center">
            <Building className="w-10 h-10 mb-1 opacity-40 text-amber-400" />
            <span className="text-[10px] text-slate-400 font-semibold">Fotografía del Inmueble</span>
            {isEditable && (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="mt-2 px-2.5 py-1 bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 rounded-lg text-[10px] font-bold flex items-center gap-1 transition-all cursor-pointer"
              >
                <Upload className="w-3 h-3" />
                <span>Adjuntar Imagen</span>
              </button>
            )}
          </div>
        )}

        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleImageUpload}
          className="hidden"
        />
      </div>

      {/* Financial Deed Specs (Rents & Price) */}
      <div className="p-3 bg-slate-950 flex flex-col gap-2 text-xs">
        {/* Purchase Price */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-1.5">
          <span className="text-slate-400 font-bold text-[11px]">Precio de Compra:</span>
          {isEditable ? (
            <div className="flex items-center gap-1 w-24">
              <span className="text-amber-400 font-mono text-xs font-bold">$</span>
              <input
                type="number"
                min="100"
                step="100"
                value={property.price}
                onChange={e => onUpdate?.({ ...property, price: parseInt(e.target.value) || 0 })}
                className="w-full bg-slate-900 border border-slate-700 rounded px-1.5 py-0.5 text-xs font-mono font-bold text-amber-400 text-right focus:outline-none"
              />
            </div>
          ) : (
            <span className="font-mono font-black text-amber-400 text-sm">
              ${property.price.toLocaleString()}
            </span>
          )}
        </div>

        {/* Base Rent */}
        <div className="flex items-center justify-between text-[11px]">
          <span className="text-slate-400">Renta Terreno Solo:</span>
          {isEditable ? (
            <div className="flex items-center gap-1 w-20">
              <span className="text-slate-400 font-mono text-xs">$</span>
              <input
                type="number"
                min="10"
                step="50"
                value={property.baseRent}
                onChange={e => onUpdate?.({ ...property, baseRent: parseInt(e.target.value) || 0 })}
                className="w-full bg-slate-900 border border-slate-700 rounded px-1.5 py-0.5 text-xs font-mono text-white text-right focus:outline-none"
              />
            </div>
          ) : (
            <span className="font-mono font-bold text-slate-200">
              ${property.baseRent.toLocaleString()}
            </span>
          )}
        </div>

        {/* Estimated House Rent */}
        <div className="flex items-center justify-between text-[10px] text-slate-400">
          <span>Con 1 Casa:</span>
          <span className="font-mono text-slate-300">
            ${(property.rentPerHouse || property.baseRent * 3).toLocaleString()}
          </span>
        </div>

        {/* Owner Status */}
        <div className="pt-1.5 border-t border-slate-800 flex items-center justify-between text-[11px]">
          <span className="text-slate-500">Estado:</span>
          {ownerName ? (
            <span className="text-amber-300 font-bold truncate">Dueño: {ownerName}</span>
          ) : (
            <span className="text-emerald-400 font-semibold">Disponible</span>
          )}
        </div>
      </div>

      {/* Buy Button if active */}
      {showBuyButton && onBuy && (
        <div className="p-2 bg-slate-900 border-t border-slate-800">
          <button
            onClick={onBuy}
            disabled={!canBuy}
            className="w-full py-2 bg-amber-500 hover:bg-amber-400 disabled:opacity-40 text-slate-950 font-black text-xs rounded-xl shadow-lg transition-all cursor-pointer"
          >
            {canBuy ? `Comprar por $${property.price.toLocaleString()}` : 'Fondos Insuficientes'}
          </button>
        </div>
      )}

      {/* Delete button in editable mode */}
      {isEditable && onDelete && (
        <div className="p-2 bg-slate-900 border-t border-slate-800 flex justify-end">
          <button
            type="button"
            onClick={onDelete}
            className="text-xs text-rose-400 hover:text-rose-300 flex items-center gap-1 font-bold p-1 transition-colors cursor-pointer"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Eliminar</span>
          </button>
        </div>
      )}
    </div>
  );
};
