import React, { useRef } from 'react';
import { 
  X, Package, Upload, Play, Check, Sparkles, Layers, Building, 
  CreditCard, ArrowRight, ExternalLink 
} from 'lucide-react';
import { ModPack, RoomState } from '../types/game';
import { modPackService } from '../services/modPackService';
import { soundFx } from '../services/soundService';

interface PackSelectorModalProps {
  currentRoom: RoomState;
  onClose: () => void;
  onSelectPack: (pack: ModPack) => void;
  onOpenStudio: () => void;
}

export const PackSelectorModal: React.FC<PackSelectorModalProps> = ({
  currentRoom,
  onClose,
  onSelectPack,
  onOpenStudio
}) => {
  const packs = modPackService.getAllPacks();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const loaded = await modPackService.importPackFromFile(file);
      soundFx.playPassGo();
      onSelectPack(loaded);
      onClose();
    } catch (err) {
      alert('Error al cargar el paquete: ' + (err as Error).message);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4 overflow-y-auto animate-in fade-in duration-200">
      <div className="bg-slate-900 border-2 border-amber-500/40 rounded-3xl p-6 sm:p-8 max-w-2xl w-full shadow-2xl flex flex-col gap-6 relative my-auto">
        
        {/* Header */}
        <div className="flex items-start justify-between gap-4 border-b border-slate-800 pb-4">
          <div className="flex items-center gap-3.5">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-amber-500 to-amber-600 flex items-center justify-center text-slate-950 font-black text-2xl shadow-lg shadow-amber-500/30">
              📦
            </div>
            <div>
              <h2 className="text-xl font-black text-white">Paquetes y ModPacks de Juego</h2>
              <p className="text-xs text-slate-400">
                Carga un juego completo con su tablero 3D, billetes, avatares, propiedades y cartas de trampa ya configurados.
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800 text-slate-400 hover:text-white transition-all cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Action Buttons: Upload .fmpack or Open Studio */}
        <div className="flex flex-col sm:flex-row items-center gap-3">
          <input
            ref={fileInputRef}
            type="file"
            accept=".json,.fmpack"
            onChange={handleFileUpload}
            className="hidden"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            className="w-full sm:flex-1 py-3 px-4 bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs rounded-xl border border-slate-700 flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            <Upload className="w-4 h-4 text-amber-400" />
            <span>Cargar Archivo de Juego (.fmpack)</span>
          </button>

          <button
            onClick={() => {
              onClose();
              onOpenStudio();
            }}
            className="w-full sm:flex-1 py-3 px-4 bg-amber-500/10 hover:bg-amber-500/20 text-amber-300 font-bold text-xs rounded-xl border border-amber-500/30 flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            <ExternalLink className="w-4 h-4" />
            <span>Diseñar Pack en Modo Estudio</span>
          </button>
        </div>

        {/* Packs Roster */}
        <div>
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-3">
            Juegos Listos para Jugar ({packs.length}):
          </span>

          <div className="flex flex-col gap-3 max-h-80 overflow-y-auto pr-1">
            {packs.map(pack => (
              <div
                key={pack.id}
                className="bg-slate-950/80 border border-slate-800 hover:border-amber-400/60 p-4 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-all group"
              >
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <h4 className="text-base font-black text-white">{pack.name}</h4>
                    {pack.isRelease && (
                      <span className="text-[10px] bg-emerald-500/20 text-emerald-300 font-bold px-2 py-0.5 rounded font-mono">
                        RELEASE
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-400 mt-1 line-clamp-2">
                    {pack.description || `Creado por ${pack.author}. Moneda: ${pack.currencyName} (${pack.currencySymbol})`}
                  </p>
                  <div className="flex flex-wrap items-center gap-3 text-[11px] text-slate-500 mt-2 font-mono">
                    <span>🏢 {pack.properties.length} Propiedades</span>
                    <span>•</span>
                    <span>⭐ {pack.cards.length} Cartas</span>
                    <span>•</span>
                    <span>💵 {pack.bills.length} Billetes</span>
                  </div>
                </div>

                <button
                  onClick={() => {
                    soundFx.playPassGo();
                    onSelectPack(pack);
                    onClose();
                  }}
                  className="w-full sm:w-auto px-5 py-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-xs rounded-xl shadow-lg shadow-amber-500/20 flex items-center justify-center gap-1.5 transition-all cursor-pointer active:scale-95"
                >
                  <Play className="w-3.5 h-3.5 fill-current" />
                  <span>Cargar a la Mesa</span>
                </button>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
};
