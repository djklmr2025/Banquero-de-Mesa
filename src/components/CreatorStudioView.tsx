import React, { useState } from 'react';
import { 
  Palette, Download, Upload, Plus, Trash2, Check, Sparkles, ArrowLeft, RefreshCw 
} from 'lucide-react';
import { BillTemplate, BoardProperty, ModPack, SurpriseCard } from '../types/game';
import { DEFAULT_BILLS, DEFAULT_CARDS, DEFAULT_PROPERTIES } from '../data/defaultGameData';

interface CreatorStudioViewProps {
  onBackToHost: () => void;
  onApplyModPack: (pack: ModPack) => void;
}

export const CreatorStudioView: React.FC<CreatorStudioViewProps> = ({
  onBackToHost,
  onApplyModPack
}) => {
  const [packName, setPackName] = useState('Edición México Clásico');
  const [authorName, setAuthorName] = useState('Fotorama Studio');
  const [bills, setBills] = useState<BillTemplate[]>(DEFAULT_BILLS);
  const [cards, setCards] = useState<SurpriseCard[]>(DEFAULT_CARDS);
  const [properties, setProperties] = useState<BoardProperty[]>(DEFAULT_PROPERTIES);

  // Active editing bill
  const [editingBillIndex, setEditingBillIndex] = useState<number>(0);
  const currentBill = bills[editingBillIndex] || bills[0];

  const handleUpdateCurrentBill = (updates: Partial<BillTemplate>) => {
    setBills(prev => prev.map((b, idx) => idx === editingBillIndex ? { ...b, ...updates } : b));
  };

  // Export JSON file
  const handleExportPack = () => {
    const pack: ModPack = {
      id: `pack_${Date.now()}`,
      name: packName,
      author: authorName,
      version: '1.0.0',
      currencyName: 'Pesos FM',
      currencySymbol: '$',
      bills,
      properties,
      cards
    };

    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(pack, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `${packName.toLowerCase().replace(/\s+/g, '_')}.fmpack.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  // Apply to active game
  const handleApply = () => {
    const pack: ModPack = {
      id: `pack_${Date.now()}`,
      name: packName,
      author: authorName,
      version: '1.0.0',
      currencyName: 'Pesos FM',
      currencySymbol: '$',
      bills,
      properties,
      cards
    };
    onApplyModPack(pack);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-4 md:p-8 flex flex-col gap-6 max-w-6xl mx-auto">
      {/* Header */}
      <header className="flex flex-wrap items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-2xl">
        <div className="flex items-center gap-4">
          <button
            onClick={onBackToHost}
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition-all flex items-center gap-2 text-sm font-bold"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Volver al Tablero</span>
          </button>
          <div>
            <h1 className="text-xl font-black text-white flex items-center gap-2">
              <Palette className="w-6 h-6 text-amber-400" />
              FM Creator Studio • Diseñador de Billetes y Reglas
            </h1>
            <p className="text-xs text-slate-400">Personaliza billetes, cartas de trampa y reglas para cualquier juego de mesa</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleExportPack}
            className="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-white text-xs font-bold rounded-xl border border-slate-700 flex items-center gap-2 transition-all cursor-pointer"
          >
            <Download className="w-4 h-4 text-amber-400" />
            <span>Exportar .fmpack</span>
          </button>

          <button
            onClick={handleApply}
            className="px-4 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-black rounded-xl shadow-lg shadow-amber-500/20 flex items-center gap-2 transition-all cursor-pointer"
          >
            <Check className="w-4 h-4" />
            <span>Aplicar a la Mesa</span>
          </button>
        </div>
      </header>

      {/* Main Studio Grid: Left Live Preview & Right Customizer */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* LEFT: Live Bill Preview & Bill Selector */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          {/* Bill Preview Card */}
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl flex flex-col items-center justify-center">
            <span className="text-xs text-slate-400 uppercase tracking-widest font-bold mb-4">
              Vista Previa en Tiempo Real
            </span>

            {/* The Actual Bill Design */}
            <div className={`w-full max-w-lg aspect-[2/1] rounded-3xl bg-gradient-to-r ${currentBill.color} p-6 relative overflow-hidden shadow-2xl border-2 border-white/30 flex flex-col justify-between text-white select-none`}>
              {/* Watermark effect */}
              <div className="absolute inset-0 watermark pointer-events-none"></div>

              {/* Top Row: Denomination and Stamp */}
              <div className="flex items-start justify-between relative z-10">
                <div>
                  <span className="text-xs font-mono tracking-widest uppercase font-bold px-2 py-0.5 bg-black/20 rounded-md border border-white/20">
                    {currentBill.stampText}
                  </span>
                  <h3 className="text-3xl font-black tracking-tight mt-1">{currentBill.label}</h3>
                </div>
                <div className="text-4xl font-black font-mono tracking-tighter opacity-90 drop-shadow">
                  ${currentBill.denomination.toLocaleString()}
                </div>
              </div>

              {/* Center Seal */}
              <div className="self-center flex flex-col items-center justify-center relative z-10">
                <div className="w-16 h-16 rounded-full border-2 border-dashed border-white/40 flex items-center justify-center bg-white/10 backdrop-blur-sm">
                  <Sparkles className="w-8 h-8 text-amber-300 animate-pulse" />
                </div>
                <span className="text-[10px] uppercase font-mono tracking-widest mt-1 opacity-75">FOTORAMA SELLO DIGITAL</span>
              </div>

              {/* Bottom Row: Signature & Denomination */}
              <div className="flex items-end justify-between relative z-10 border-t border-white/20 pt-3">
                <div>
                  <span className="text-[10px] opacity-75 block">Firma del Banquero:</span>
                  <span className="font-serif italic text-base tracking-wider">{currentBill.signature}</span>
                </div>
                <div className="text-2xl font-black font-mono tracking-tighter opacity-80">
                  ${currentBill.denomination.toLocaleString()}
                </div>
              </div>
            </div>

            {/* Bill Selector Chips */}
            <div className="flex flex-wrap gap-2 justify-center mt-6">
              {bills.map((b, idx) => (
                <button
                  key={b.denomination}
                  onClick={() => setEditingBillIndex(idx)}
                  className={`px-3 py-2 rounded-xl text-xs font-bold transition-all ${
                    idx === editingBillIndex
                      ? 'bg-amber-500 text-slate-950 font-black shadow-lg shadow-amber-500/20'
                      : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  }`}
                >
                  ${b.denomination.toLocaleString()}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* RIGHT: Customizer Controls */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl flex flex-col gap-4">
            <h3 className="text-base font-bold text-white">Propiedades del Billete</h3>

            <div>
              <label className="text-xs text-slate-400 font-bold block mb-1">Denominación Facial ($):</label>
              <input
                type="number"
                value={currentBill.denomination}
                onChange={e => handleUpdateCurrentBill({ denomination: parseInt(e.target.value) || 0 })}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-sm text-white font-mono font-bold"
              />
            </div>

            <div>
              <label className="text-xs text-slate-400 font-bold block mb-1">Nombre / Etiqueta:</label>
              <input
                type="text"
                value={currentBill.label}
                onChange={e => handleUpdateCurrentBill({ label: e.target.value })}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-sm text-white font-medium"
              />
            </div>

            <div>
              <label className="text-xs text-slate-400 font-bold block mb-1">Sello Oficial (Fotorama / Banco):</label>
              <input
                type="text"
                value={currentBill.stampText}
                onChange={e => handleUpdateCurrentBill({ stampText: e.target.value })}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-sm text-white font-medium"
              />
            </div>

            <div>
              <label className="text-xs text-slate-400 font-bold block mb-1">Firma:</label>
              <input
                type="text"
                value={currentBill.signature}
                onChange={e => handleUpdateCurrentBill({ signature: e.target.value })}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-sm text-white font-medium"
              />
            </div>

            {/* Gradient Preset Selector */}
            <div>
              <label className="text-xs text-slate-400 font-bold block mb-2">Paleta de Color Gradiente:</label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { name: 'Oro Clásico', gradient: 'from-amber-600 to-amber-700' },
                  { name: 'Esmeralda', gradient: 'from-emerald-600 to-emerald-800' },
                  { name: 'Azul Real', gradient: 'from-cyan-600 to-blue-800' },
                  { name: 'Púrpura Imperial', gradient: 'from-purple-600 to-indigo-800' },
                  { name: 'Rubí', gradient: 'from-rose-600 to-pink-800' },
                  { name: 'Diamante Negro', gradient: 'from-slate-800 to-slate-950' }
                ].map(p => (
                  <button
                    key={p.name}
                    type="button"
                    onClick={() => handleUpdateCurrentBill({ color: p.gradient })}
                    className={`p-2 rounded-xl border text-[11px] font-bold text-center transition-all ${
                      currentBill.color === p.gradient ? 'border-amber-400 bg-slate-800 text-white' : 'border-slate-800 bg-slate-950 text-slate-400'
                    }`}
                  >
                    <div className={`h-3 rounded-md bg-gradient-to-r ${p.gradient} mb-1.5`}></div>
                    {p.name}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
