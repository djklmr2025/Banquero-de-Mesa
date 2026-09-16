import React, { useState, useRef } from 'react';
import { 
  Palette, Download, Upload, Plus, Trash2, Check, Sparkles, ArrowLeft, RefreshCw,
  Building, CreditCard, ShieldAlert, Image as ImageIcon, Layers, FileArchive, Edit3, Play
} from 'lucide-react';
import { BillTemplate, BoardProperty, ModPack, SurpriseCard } from '../types/game';
import { DEFAULT_BILLS, DEFAULT_CARDS, DEFAULT_PROPERTIES } from '../data/defaultGameData';
import { BUILTIN_PACKS, modPackService } from '../services/modPackService';
import { VerticalPropertyCard } from './VerticalPropertyCard';
import { SurpriseCardView } from './SurpriseCardView';
import { soundFx } from '../services/soundService';

interface CreatorStudioViewProps {
  onBackToHost: () => void;
  onApplyModPack: (pack: ModPack) => void;
}

export const CreatorStudioView: React.FC<CreatorStudioViewProps> = ({
  onBackToHost,
  onApplyModPack
}) => {
  const [activeTab, setActiveTab] = useState<'general' | 'bills' | 'properties' | 'cards'>('general');
  const [packId, setPackId] = useState<string>(() => `pack_${Date.now()}`);
  const [packName, setPackName] = useState('Mi Juego de Mesa Personalizado');
  const [authorName, setAuthorName] = useState('Creador FM');
  const [version, setVersion] = useState('1.0.0');
  const [currencyName, setCurrencyName] = useState('Pesos FM');
  const [currencySymbol, setCurrencySymbol] = useState('$');
  const [boardImage, setBoardImage] = useState<string | undefined>(undefined);
  const [playerSpins, setPlayerSpins] = useState<string[]>(['🎩', '🏎️', '🚀', '👑', '🐶', '🐱', '⚽', '🍕']);
  const [bills, setBills] = useState<BillTemplate[]>(DEFAULT_BILLS);
  const [cards, setCards] = useState<SurpriseCard[]>(DEFAULT_CARDS);
  const [properties, setProperties] = useState<BoardProperty[]>(DEFAULT_PROPERTIES);
  const [isRelease, setIsRelease] = useState<boolean>(false);
  const [saveSuccessMsg, setSaveSuccessMsg] = useState<string | null>(null);

  // Active bill editing
  const [editingBillIndex, setEditingBillIndex] = useState<number>(0);
  const currentBill = bills[editingBillIndex] || bills[0];

  const boardImageInputRef = useRef<HTMLInputElement>(null);
  const packFileInputRef = useRef<HTMLInputElement>(null);

  // Handle board image upload
  const handleBoardImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      setBoardImage(reader.result as string);
      soundFx.playCoin();
    };
    reader.readAsDataURL(file);
  };

  // Build current pack object
  const getCurrentPack = (asRelease: boolean = isRelease): ModPack => {
    return {
      id: packId,
      name: packName.trim() || 'Juego Personalizado',
      author: authorName.trim() || 'Creador',
      version,
      currencyName,
      currencySymbol,
      boardImage,
      playerSpins,
      bills,
      properties,
      cards,
      isRelease: asRelease,
      updatedAt: Date.now()
    };
  };

  // Close pack as release
  const handleClosePackageRelease = () => {
    soundFx.playPassGo();
    const releasePack = getCurrentPack(true);
    setIsRelease(true);
    modPackService.savePack(releasePack);
    setSaveSuccessMsg('¡Paquete de juego cerrado con éxito! Ha quedado guardado como Release listo para ocupar.');
    setTimeout(() => setSaveSuccessMsg(null), 4000);
  };

  // Reopen for editing
  const handleReopenForEditing = () => {
    setIsRelease(false);
    const draftPack = getCurrentPack(false);
    modPackService.savePack(draftPack);
  };

  // Export pack file (.fmpack)
  const handleExportPack = () => {
    const pack = getCurrentPack();
    modPackService.exportPackToFile(pack);
  };

  // Import pack file (.fmpack)
  const handleImportPackFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const loaded = await modPackService.importPackFromFile(file);
      loadPackIntoStudio(loaded);
      soundFx.playPassGo();
      setSaveSuccessMsg(`¡Paquete "${loaded.name}" cargado exitosamente!`);
      setTimeout(() => setSaveSuccessMsg(null), 3000);
    } catch (err) {
      alert('Error al importar el paquete: ' + (err as Error).message);
    }
  };

  // Load any pack into state
  const loadPackIntoStudio = (pack: ModPack) => {
    setPackId(pack.id);
    setPackName(pack.name);
    setAuthorName(pack.author);
    setVersion(pack.version || '1.0.0');
    setCurrencyName(pack.currencyName || 'Pesos FM');
    setCurrencySymbol(pack.currencySymbol || '$');
    setBoardImage(pack.boardImage);
    setPlayerSpins(pack.playerSpins || ['🎩', '🏎️', '🚀', '👑', '🐶', '🐱', '⚽', '🍕']);
    setBills(pack.bills);
    setProperties(pack.properties);
    setCards(pack.cards);
    setIsRelease(pack.isRelease || false);
  };

  // Apply to active game
  const handleApplyToActiveGame = () => {
    const pack = getCurrentPack(true);
    modPackService.savePack(pack);
    onApplyModPack(pack);
  };

  // Property handlers
  const handleUpdateProperty = (updated: BoardProperty) => {
    setProperties(prev => prev.map(p => p.id === updated.id ? updated : p));
  };

  const handleAddProperty = () => {
    const colors = ['#8B4513', '#38BDF8', '#EC4899', '#F97316', '#EF4444', '#EAB308', '#22C55E', '#1D4ED8'];
    const newProp: BoardProperty = {
      id: `prop_${Date.now()}`,
      name: `Nueva Propiedad ${properties.length + 1}`,
      group: 'General',
      price: 1500,
      baseRent: 150,
      rentPerHouse: 450,
      houses: 0,
      ownerId: null,
      color: colors[properties.length % colors.length],
      mortgaged: false,
      description: 'Descripción e historia temática de esta propiedad.'
    };
    setProperties(prev => [...prev, newProp]);
  };

  // Card handlers
  const handleUpdateCard = (updated: SurpriseCard) => {
    setCards(prev => prev.map(c => c.id === updated.id ? updated : c));
  };

  const handleAddCard = () => {
    const newCard: SurpriseCard = {
      id: `card_${Date.now()}`,
      title: '¡Nueva Carta de Suerte o Trampa!',
      description: 'Describe aquí la regla o beneficio que recibirá el jugador.',
      type: 'RECEIVE_BANK',
      amount: 2500,
      rarity: 'rara',
      borderColor: '#F59E0B'
    };
    setCards(prev => [...prev, newCard]);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-4 md:p-8 flex flex-col gap-6 max-w-7xl mx-auto font-sans">
      {/* Top Header */}
      <header className="flex flex-wrap items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-3xl shadow-xl">
        <div className="flex items-center gap-4">
          <button
            onClick={onBackToHost}
            className="p-2.5 rounded-2xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition-all flex items-center gap-2 text-xs font-bold cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Volver a la Mesa</span>
          </button>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-xl sm:text-2xl font-black text-white flex items-center gap-2">
                <Palette className="w-6 h-6 text-amber-400" />
                <span>Modo Estudio • Creador de Paquetes de Juego</span>
              </h1>
              {isRelease ? (
                <span className="text-[10px] bg-emerald-500/20 text-emerald-300 font-mono font-bold px-2.5 py-1 rounded-full border border-emerald-500/30 flex items-center gap-1">
                  <Check className="w-3 h-3" /> Pack Release Listo
                </span>
              ) : (
                <span className="text-[10px] bg-amber-500/20 text-amber-300 font-mono font-bold px-2.5 py-1 rounded-full border border-amber-500/30 flex items-center gap-1">
                  <Edit3 className="w-3 h-3" /> En Edición
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Diseña fichas, billetes, tablero 3D, propiedades con fotos y cartas de trampa/suerte en un solo paquete comprimible.
            </p>
          </div>
        </div>

        {/* Global Pack Actions */}
        <div className="flex flex-wrap items-center gap-2.5">
          <input
            ref={packFileInputRef}
            type="file"
            accept=".json,.fmpack"
            onChange={handleImportPackFile}
            className="hidden"
          />
          <button
            onClick={() => packFileInputRef.current?.click()}
            className="px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold rounded-xl border border-slate-700 flex items-center gap-1.5 transition-all cursor-pointer"
            title="Importar paquete de juego .fmpack"
          >
            <Upload className="w-4 h-4 text-amber-400" />
            <span>Cargar Pack (.fmpack)</span>
          </button>

          <button
            onClick={handleExportPack}
            className="px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold rounded-xl border border-slate-700 flex items-center gap-1.5 transition-all cursor-pointer"
            title="Descargar archivo comprimible .fmpack"
          >
            <Download className="w-4 h-4 text-amber-400" />
            <span>Exportar Pack</span>
          </button>

          {!isRelease ? (
            <button
              onClick={handleClosePackageRelease}
              className="px-4 py-2.5 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 text-slate-950 text-xs font-black rounded-xl shadow-lg shadow-emerald-500/20 flex items-center gap-2 transition-all cursor-pointer active:scale-98"
              title="Cerrar paquete y dejarlo como release oficial listo para jugar"
            >
              <FileArchive className="w-4 h-4" />
              <span>Cerrar Paquete (Pack Release)</span>
            </button>
          ) : (
            <div className="flex items-center gap-2">
              <button
                onClick={handleReopenForEditing}
                className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-amber-300 text-xs font-bold rounded-xl border border-amber-500/30 flex items-center gap-1.5 transition-all cursor-pointer"
              >
                <Edit3 className="w-3.5 h-3.5" />
                <span>Reabrir para Editar</span>
              </button>

              <button
                onClick={handleApplyToActiveGame}
                className="px-4 py-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950 text-xs font-black rounded-xl shadow-lg shadow-amber-500/20 flex items-center gap-2 transition-all cursor-pointer"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>Jugar Ahora con este Pack</span>
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Success Notification Banner */}
      {saveSuccessMsg && (
        <div className="p-4 bg-emerald-500/20 border border-emerald-500/40 rounded-2xl text-emerald-300 text-xs font-bold flex items-center gap-2 animate-in fade-in">
          <Check className="w-5 h-5" />
          <span>{saveSuccessMsg}</span>
        </div>
      )}

      {/* Preset Packs Quick Switcher Bar */}
      <div className="bg-slate-900/90 border border-slate-800 p-3 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2 text-xs text-slate-400 font-bold">
          <span>Plantillas Rápidas Pre-Cargadas:</span>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {BUILTIN_PACKS.map(preset => (
            <button
              key={preset.id}
              onClick={() => {
                loadPackIntoStudio(preset);
                soundFx.playCoin();
              }}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer border ${
                packId === preset.id 
                  ? 'bg-amber-500 text-slate-950 border-amber-400 shadow' 
                  : 'bg-slate-950 text-slate-300 border-slate-800 hover:border-slate-700'
              }`}
            >
              {preset.name}
            </button>
          ))}
        </div>
      </div>

      {/* Main Studio Navigation Tabs */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-2 bg-slate-900 p-1.5 rounded-2xl border border-slate-800 text-xs font-bold">
        <button
          onClick={() => setActiveTab('general')}
          className={`py-3 rounded-xl transition-all flex items-center justify-center gap-2 cursor-pointer ${
            activeTab === 'general' ? 'bg-amber-500 text-slate-950 shadow-md font-black' : 'text-slate-400 hover:text-white'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>1. General y Tablero</span>
        </button>
        <button
          onClick={() => setActiveTab('bills')}
          className={`py-3 rounded-xl transition-all flex items-center justify-center gap-2 cursor-pointer ${
            activeTab === 'bills' ? 'bg-amber-500 text-slate-950 shadow-md font-black' : 'text-slate-400 hover:text-white'
          }`}
        >
          <CreditCard className="w-4 h-4" />
          <span>2. Billetes ({bills.length})</span>
        </button>
        <button
          onClick={() => setActiveTab('properties')}
          className={`py-3 rounded-xl transition-all flex items-center justify-center gap-2 cursor-pointer ${
            activeTab === 'properties' ? 'bg-amber-500 text-slate-950 shadow-md font-black' : 'text-slate-400 hover:text-white'
          }`}
        >
          <Building className="w-4 h-4" />
          <span>3. Propiedades y Fotos ({properties.length})</span>
        </button>
        <button
          onClick={() => setActiveTab('cards')}
          className={`py-3 rounded-xl transition-all flex items-center justify-center gap-2 cursor-pointer ${
            activeTab === 'cards' ? 'bg-amber-500 text-slate-950 shadow-md font-black' : 'text-slate-400 hover:text-white'
          }`}
        >
          <Sparkles className="w-4 h-4" />
          <span>4. Cartas Trampa y Suerte ({cards.length})</span>
        </button>
      </div>

      {/* ============================================================== */}
      {/* TAB 1: GENERAL PACK SETTINGS & BOARD IMAGE                     */}
      {/* ============================================================== */}
      {activeTab === 'general' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Metadata Form */}
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col gap-4">
            <h3 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
              <Layers className="w-4 h-4 text-amber-400" />
              <span>Ficha Técnica del Paquete de Juego</span>
            </h3>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-400">Nombre del Juego / Pack:</label>
              <input
                type="text"
                value={packName}
                onChange={e => setPackName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm font-bold text-white focus:outline-none focus:border-amber-400"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-slate-400">Autor / Diseñador:</label>
                <input
                  type="text"
                  value={authorName}
                  onChange={e => setAuthorName(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-400"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-slate-400">Versión del Pack:</label>
                <input
                  type="text"
                  value={version}
                  onChange={e => setVersion(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm font-mono text-white focus:outline-none focus:border-amber-400"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-slate-400">Nombre de Moneda:</label>
                <input
                  type="text"
                  value={currencyName}
                  onChange={e => setCurrencyName(e.target.value)}
                  placeholder="Ej: Pesos FM, Créditos, Doblones"
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-400"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-slate-400">Símbolo Monetario:</label>
                <input
                  type="text"
                  value={currencySymbol}
                  onChange={e => setCurrencySymbol(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm font-mono font-bold text-amber-400 text-center focus:outline-none focus:border-amber-400"
                />
              </div>
            </div>

            {/* Player Spins / Avatars */}
            <div className="flex flex-col gap-2 pt-2 border-t border-slate-800">
              <label className="text-xs font-bold text-slate-400">Spins y Fichas de Jugador (Emojis/Iconos):</label>
              <div className="flex flex-wrap gap-2">
                {playerSpins.map((spin, idx) => (
                  <input
                    key={idx}
                    type="text"
                    value={spin}
                    onChange={e => {
                      const updated = [...playerSpins];
                      updated[idx] = e.target.value;
                      setPlayerSpins(updated);
                    }}
                    className="w-10 h-10 bg-slate-950 border border-slate-700 rounded-xl text-center text-lg focus:outline-none focus:border-amber-400"
                  />
                ))}
              </div>
            </div>
          </div>

          {/* Board Image Attachment */}
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col justify-between gap-4">
            <div>
              <h3 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2 mb-1">
                <ImageIcon className="w-4 h-4 text-amber-400" />
                <span>Textura / Imagen del Tablero 3D</span>
              </h3>
              <p className="text-xs text-slate-400 mb-4">
                Sube una imagen o arte para que se proyecte en el escenario 3D inclinado. Si no se sube, se usará el tablero virtual interactivo de Fotorama.
              </p>

              <input
                ref={boardImageInputRef}
                type="file"
                accept="image/*"
                onChange={handleBoardImageUpload}
                className="hidden"
              />

              <div className="relative aspect-video rounded-2xl bg-slate-950 border-2 border-dashed border-slate-700 overflow-hidden flex items-center justify-center">
                {boardImage ? (
                  <>
                    <img src={boardImage} alt="Tablero 3D" className="w-full h-full object-cover" />
                    <div className="absolute top-3 right-3 flex items-center gap-2">
                      <button
                        onClick={() => boardImageInputRef.current?.click()}
                        className="px-3 py-1.5 bg-slate-900/90 hover:bg-slate-800 text-white rounded-xl text-xs font-bold border border-slate-700 cursor-pointer"
                      >
                        Cambiar
                      </button>
                      <button
                        onClick={() => setBoardImage(undefined)}
                        className="p-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs cursor-pointer"
                        title="Quitar imagen"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </>
                ) : (
                  <div className="flex flex-col items-center justify-center p-6 text-center">
                    <ImageIcon className="w-12 h-12 text-slate-600 mb-2" />
                    <span className="text-xs font-bold text-slate-300">Sin tablero personalizado adjunto</span>
                    <span className="text-[11px] text-slate-500 mt-1">Soporta PNG, JPG, WebP en alta resolución</span>
                    <button
                      onClick={() => boardImageInputRef.current?.click()}
                      className="mt-3 px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs rounded-xl shadow transition-all cursor-pointer"
                    >
                      Adjuntar Imagen de Tablero
                    </button>
                  </div>
                )}
              </div>
            </div>

            <div className="p-3.5 bg-slate-950/80 rounded-2xl border border-slate-800 text-xs text-slate-400 flex items-center justify-between">
              <span>El tablero se integrará automáticamente al exportar el <strong>.fmpack</strong>.</span>
            </div>
          </div>
        </div>
      )}

      {/* ============================================================== */}
      {/* TAB 2: BILLS DESIGNER                                          */}
      {/* ============================================================== */}
      {activeTab === 'bills' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div className="lg:col-span-7 flex flex-col gap-6">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl flex flex-col items-center justify-center">
              <span className="text-xs text-slate-400 uppercase tracking-widest font-bold mb-4">
                Previsualización en Vivo del Billete
              </span>

              {/* The Actual Bill Design */}
              <div className={`w-full max-w-lg aspect-[2/1] rounded-3xl bg-gradient-to-r ${currentBill.color} p-6 relative overflow-hidden shadow-2xl border-2 border-white/30 flex flex-col justify-between text-white select-none`}>
                <div className="flex items-start justify-between relative z-10">
                  <div>
                    <span className="text-xs font-mono tracking-widest uppercase font-bold px-2 py-0.5 bg-black/20 rounded-md border border-white/20">
                      {currentBill.stampText}
                    </span>
                    <h3 className="text-3xl font-black tracking-tight mt-1">{currentBill.label}</h3>
                  </div>
                  <div className="text-4xl font-black font-mono tracking-tighter opacity-90 drop-shadow">
                    {currencySymbol}{currentBill.denomination.toLocaleString()}
                  </div>
                </div>

                <div className="self-center flex flex-col items-center justify-center relative z-10">
                  <div className="w-16 h-16 rounded-full border-2 border-dashed border-white/40 flex items-center justify-center bg-white/10 backdrop-blur-sm">
                    <Sparkles className="w-8 h-8 text-amber-300 animate-pulse" />
                  </div>
                  <span className="text-[10px] uppercase font-mono tracking-widest mt-1 opacity-75">{currentBill.signature}</span>
                </div>

                <div className="flex items-end justify-between relative z-10 border-t border-white/20 pt-3">
                  <div>
                    <span className="text-[10px] opacity-75 block">Firma del Banquero:</span>
                    <span className="font-serif italic text-base tracking-wider">{currentBill.signature}</span>
                  </div>
                  <div className="text-2xl font-black font-mono tracking-tighter opacity-80">
                    {currencySymbol}{currentBill.denomination.toLocaleString()}
                  </div>
                </div>
              </div>

              {/* Bill Selector Chips */}
              <div className="flex flex-wrap gap-2 justify-center mt-6">
                {bills.map((b, idx) => (
                  <button
                    key={b.denomination}
                    onClick={() => setEditingBillIndex(idx)}
                    className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                      idx === editingBillIndex
                        ? 'bg-amber-500 text-slate-950 font-black shadow-lg shadow-amber-500/20'
                        : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                    }`}
                  >
                    {currencySymbol}{b.denomination.toLocaleString()}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Bill Properties Editor */}
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col gap-4">
            <h3 className="text-base font-bold text-white uppercase tracking-wider flex items-center gap-2">
              <CreditCard className="w-4 h-4 text-amber-400" />
              <span>Editar Billete de {currencySymbol}{currentBill.denomination.toLocaleString()}</span>
            </h3>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-400">Texto / Denominación Escrita:</label>
              <input
                type="text"
                value={currentBill.label}
                onChange={e => {
                  const updated = [...bills];
                  updated[editingBillIndex] = { ...currentBill, label: e.target.value };
                  setBills(updated);
                }}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-400"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-400">Texto del Sello Superior:</label>
              <input
                type="text"
                value={currentBill.stampText}
                onChange={e => {
                  const updated = [...bills];
                  updated[editingBillIndex] = { ...currentBill, stampText: e.target.value };
                  setBills(updated);
                }}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-400"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-slate-400">Firma del Banquero Emisor:</label>
              <input
                type="text"
                value={currentBill.signature}
                onChange={e => {
                  const updated = [...bills];
                  updated[editingBillIndex] = { ...currentBill, signature: e.target.value };
                  setBills(updated);
                }}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-400"
              />
            </div>
          </div>
        </div>
      )}

      {/* ============================================================== */}
      {/* TAB 3: PROPERTIES WITH VERTICAL IMAGES & TEXT                  */}
      {/* ============================================================== */}
      {activeTab === 'properties' && (
        <div className="flex flex-col gap-6">
          <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-900 border border-slate-800 p-4 rounded-2xl">
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Building className="w-5 h-5 text-amber-400" />
                <span>Propiedades y Escrituras Verticales ({properties.length})</span>
              </h3>
              <p className="text-xs text-slate-400">
                Añade el nombre, cantidad de compra, rentas, fotos verticales y notas temáticas para cada inmueble.
              </p>
            </div>

            <button
              onClick={handleAddProperty}
              className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs rounded-xl shadow flex items-center gap-1.5 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Nueva Propiedad</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 max-h-[600px] overflow-y-auto pr-1">
            {properties.map(prop => (
              <div key={prop.id} className="flex justify-center">
                <VerticalPropertyCard
                  property={prop}
                  isEditable={true}
                  onUpdate={handleUpdateProperty}
                  onDelete={() => setProperties(prev => prev.filter(p => p.id !== prop.id))}
                />
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ============================================================== */}
      {/* TAB 4: TRAP & LUCK CARDS WITH BACKGROUND IMAGE & BORDER COLOR  */}
      {/* ============================================================== */}
      {activeTab === 'cards' && (
        <div className="flex flex-col gap-6">
          <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-900 border border-slate-800 p-4 rounded-2xl">
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-amber-400" />
                <span>Baraja de Cartas Sorpresa, Trampa y Suerte ({cards.length})</span>
              </h3>
              <p className="text-xs text-slate-400">
                Personaliza texto, imagen de fondo completa y color de contorno para cada carta de la baraja.
              </p>
            </div>

            <button
              onClick={handleAddCard}
              className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs rounded-xl shadow flex items-center gap-1.5 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Nueva Carta</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5 max-h-[600px] overflow-y-auto pr-1 justify-items-center">
            {cards.map(c => (
              <SurpriseCardView
                key={c.id}
                card={c}
                isEditable={true}
                onUpdate={handleUpdateCard}
                onDelete={() => setCards(prev => prev.filter(x => x.id !== c.id))}
              />
            ))}
          </div>
        </div>
      )}

    </div>
  );
};
