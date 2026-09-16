import React, { useState, useRef } from 'react';
import { 
  Upload, Image as ImageIcon, RotateCw, Eye, Maximize2, Minimize2, 
  Trash2, Sparkles, MapPin, Layers, Building
} from 'lucide-react';
import { BoardProperty, Player, RoomState } from '../types/game';
import { soundFx } from '../services/soundService';

interface ThreeDBoardStageProps {
  roomState: RoomState;
  onUpdateRoom: (newState: RoomState) => void;
  onSelectProperty?: (propertyId: string) => void;
}

export const ThreeDBoardStage: React.FC<ThreeDBoardStageProps> = ({
  roomState,
  onUpdateRoom,
  onSelectProperty
}) => {
  const [viewAngle, setViewAngle] = useState<'3d' | 'flat' | 'cinematic'>('3d');
  const [zoomLevel, setZoomLevel] = useState<number>(1);
  const [isExpanded, setIsExpanded] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Handle board image upload
  const handleUploadBoardImage = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert('Por favor selecciona un archivo de imagen válido (PNG, JPG, WebP).');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      onUpdateRoom({
        ...roomState,
        customBoardImage: dataUrl
      });
      soundFx.playCoin();
    };
    reader.readAsDataURL(file);
  };

  // Remove custom board image
  const handleRemoveBoardImage = () => {
    onUpdateRoom({
      ...roomState,
      customBoardImage: undefined
    });
  };

  // 3D transform style based on viewAngle
  const getBoardTransform = () => {
    if (viewAngle === 'flat') {
      return `rotateX(0deg) rotateZ(0deg) scale(${zoomLevel})`;
    }
    if (viewAngle === 'cinematic') {
      return `rotateX(62deg) rotateZ(-22deg) scale(${zoomLevel * 1.05})`;
    }
    // Default isometric 3D tilt
    return `rotateX(50deg) rotateZ(-12deg) scale(${zoomLevel})`;
  };

  // Total board slots: properties length + 2 (GO at 0, Jail at middle)
  const totalSlots = Math.max(roomState.properties.length + 2, 12);

  // Compute position for each player on a square perimeter
  const getPlayerSlotCoordinates = (position: number) => {
    const normPos = position % totalSlots;
    const sideSlots = Math.ceil(totalSlots / 4);
    const side = Math.floor(normPos / sideSlots);
    const offset = normPos % sideSlots;
    const pct = (offset / sideSlots) * 80 + 10;

    switch (side) {
      case 0: // Bottom row (Left to Right)
        return { bottom: '5%', left: `${pct}%` };
      case 1: // Right column (Bottom to Top)
        return { right: '5%', bottom: `${pct}%` };
      case 2: // Top row (Right to Left)
        return { top: '5%', right: `${pct}%` };
      case 3: // Left column (Top to Bottom)
      default:
        return { left: '5%', top: `${pct}%` };
    }
  };

  return (
    <div className={`bg-slate-900 border border-slate-800 rounded-3xl p-5 sm:p-7 shadow-2xl flex flex-col gap-5 transition-all ${
      isExpanded ? 'fixed inset-4 z-50 bg-slate-950/95 backdrop-blur-xl overflow-hidden' : ''
    }`}>
      {/* Stage Header Controls */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400">
            <Layers className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base sm:text-lg font-black text-white">Escenario y Tablero 3D en Vivo</h3>
              <span className="text-[10px] bg-amber-500/20 text-amber-300 font-mono font-bold px-2 py-0.5 rounded-full border border-amber-500/30">
                Inclinación Isométrica
              </span>
            </div>
            <p className="text-xs text-slate-400">
              Vista tridimensional física con fichas en tiempo real. Adjunta tu propio diseño de tablero físico o digital.
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Angle buttons */}
          <div className="flex items-center bg-slate-950 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setViewAngle('3d')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                viewAngle === '3d' ? 'bg-amber-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
              }`}
            >
              📐 3D Inclinado
            </button>
            <button
              onClick={() => setViewAngle('cinematic')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                viewAngle === 'cinematic' ? 'bg-amber-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
              }`}
            >
              🎬 Cinemático
            </button>
            <button
              onClick={() => setViewAngle('flat')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                viewAngle === 'flat' ? 'bg-amber-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
              }`}
            >
              🔍 Plano
            </button>
          </div>

          {/* Upload Custom Board Image Button */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleUploadBoardImage}
            className="hidden"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-xs rounded-xl shadow-lg shadow-amber-500/20 transition-all cursor-pointer"
            title="Sube una foto o render de tu tablero físico"
          >
            <Upload className="w-3.5 h-3.5" />
            <span>Adjuntar Tablero</span>
          </button>

          {roomState.customBoardImage && (
            <button
              onClick={handleRemoveBoardImage}
              className="p-2 bg-slate-800 hover:bg-rose-950/60 text-slate-400 hover:text-rose-400 border border-slate-700 rounded-xl transition-all cursor-pointer"
              title="Restablecer tablero predeterminado"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}

          {/* Fullscreen Expand toggle */}
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl border border-slate-700 transition-all cursor-pointer"
            title={isExpanded ? 'Contraer' : 'Expandir pantalla completa'}
          >
            {isExpanded ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* 3D Viewport Stage Container */}
      <div 
        className="relative w-full h-[460px] sm:h-[540px] bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950 rounded-3xl border border-slate-800/80 overflow-hidden flex items-center justify-center select-none shadow-inner"
        style={{ perspective: '1100px' }}
      >
        {/* Subtle Felt/Wood Table Ambience Texture */}
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(245,158,11,0.08)_0%,transparent_70%)] pointer-events-none"></div>

        {/* 3D Board Plane with Perspective Tilt */}
        <div 
          className="relative w-[340px] sm:w-[480px] md:w-[540px] aspect-square rounded-3xl transition-transform duration-700 ease-out shadow-[0_35px_60px_-15px_rgba(0,0,0,0.9)] border-8 border-slate-800"
          style={{
            transform: getBoardTransform(),
            transformStyle: 'preserve-3d',
            backgroundColor: '#0F172A'
          }}
        >
          {/* Board Surface Content: Custom Image OR Default Interactive Tiles */}
          {roomState.customBoardImage ? (
            <div className="absolute inset-0 rounded-2xl overflow-hidden shadow-inner">
              <img
                src={roomState.customBoardImage}
                alt="Tablero de Mesa Personalizado"
                className="w-full h-full object-cover rounded-2xl"
              />
              <div className="absolute inset-0 bg-gradient-to-tr from-black/20 via-transparent to-white/10 pointer-events-none"></div>
            </div>
          ) : (
            /* Default Stylized Virtual Board */
            <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-slate-900 via-slate-950 to-slate-900 p-4 border border-amber-500/20 flex flex-col justify-between">
              {/* Center Board Logo & Banker Seal */}
              <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none opacity-25">
                <div className="w-32 h-32 rounded-full border-4 border-dashed border-amber-400 flex items-center justify-center">
                  <span className="text-4xl font-black text-amber-400 font-mono tracking-widest">FM</span>
                </div>
                <span className="text-xs font-black tracking-widest text-amber-300 mt-2">BANQUERO DE MESA</span>
                <span className="text-[10px] text-slate-400">Fotorama de México • Edición Digital</span>
              </div>

              {/* Perimeter Tiles Track */}
              <div className="w-full h-full border-2 border-slate-800 rounded-xl grid grid-cols-4 grid-rows-4 gap-1 p-1">
                {/* Corner 1: GO (Salida) */}
                <div className="col-start-1 row-start-4 bg-emerald-950/80 border border-emerald-500/50 rounded-lg p-2 flex flex-col items-center justify-center text-center">
                  <span className="text-xs font-black text-emerald-400">SALIDA</span>
                  <span className="text-[9px] text-emerald-300 font-mono font-bold">+$20,000</span>
                </div>

                {/* Corner 2: JAIL (Cárcel) */}
                <div className="col-start-1 row-start-1 bg-rose-950/80 border border-rose-500/50 rounded-lg p-2 flex flex-col items-center justify-center text-center">
                  <span className="text-xs font-black text-rose-400">CÁRCEL</span>
                  <span className="text-[9px] text-rose-300">Visitas</span>
                </div>

                {/* Corner 3: FREE PARKING */}
                <div className="col-start-4 row-start-1 bg-amber-950/80 border border-amber-500/50 rounded-lg p-2 flex flex-col items-center justify-center text-center">
                  <span className="text-xs font-black text-amber-400">DESCANSO</span>
                  <span className="text-[9px] text-amber-300">Parada Libre</span>
                </div>

                {/* Corner 4: GO TO JAIL */}
                <div className="col-start-4 row-start-4 bg-purple-950/80 border border-purple-500/50 rounded-lg p-2 flex flex-col items-center justify-center text-center">
                  <span className="text-xs font-black text-purple-400">A PRISIÓN</span>
                  <span className="text-[9px] text-purple-300">Sin Salida</span>
                </div>

                {/* Interactive Property Highlights */}
                {roomState.properties.slice(0, 8).map((prop, pIdx) => {
                  const owner = roomState.players.find(p => p.id === prop.ownerId);
                  return (
                    <div
                      key={prop.id}
                      onClick={() => onSelectProperty?.(prop.id)}
                      className="bg-slate-900/90 border border-slate-800 hover:border-amber-400 rounded-md p-1.5 flex flex-col justify-between cursor-pointer transition-colors"
                      title={`${prop.name} - $${prop.price.toLocaleString()}`}
                    >
                      <div className="h-1.5 rounded-full" style={{ backgroundColor: prop.color }}></div>
                      <span className="text-[9px] font-bold text-white truncate">{prop.name}</span>
                      <div className="flex items-center justify-between text-[8px]">
                        <span className="text-amber-400 font-mono font-bold">${prop.price.toLocaleString()}</span>
                        {owner && <span className="text-white">{owner.avatar}</span>}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* 3D Standing Billboard Tokens for All Players */}
          {roomState.players.map(player => {
            const coords = getPlayerSlotCoordinates(player.position);
            const isTurn = player.id === roomState.currentTurnPlayerId;

            return (
              <div
                key={player.id}
                className="absolute transition-all duration-700 ease-out z-20"
                style={{
                  ...coords,
                  transform: viewAngle === 'flat' ? 'none' : 'rotateX(-50deg) translateZ(25px)',
                  transformStyle: 'preserve-3d'
                }}
              >
                {/* 3D Token Billboard */}
                <div className="flex flex-col items-center">
                  {/* Active turn halo */}
                  {isTurn && (
                    <span className="text-[9px] bg-amber-400 text-slate-950 font-black px-1.5 py-0.2 rounded-full shadow-lg mb-0.5 animate-bounce">
                      TURNO
                    </span>
                  )}

                  {/* Standing Token */}
                  <div 
                    className={`w-10 h-10 rounded-2xl flex items-center justify-center text-xl shadow-2xl border-2 transition-transform ${
                      isTurn 
                        ? 'scale-125 border-amber-400 shadow-amber-400/50 ring-4 ring-amber-400/20' 
                        : 'border-white/40 shadow-black/80 hover:scale-110'
                    }`}
                    style={{ backgroundColor: player.color }}
                  >
                    {player.avatar}
                  </div>

                  {/* Player Name Pill */}
                  <span className="text-[9px] font-black text-white bg-slate-950/90 px-1.5 py-0.5 rounded-md border border-slate-800 shadow-md mt-0.5 truncate max-w-[70px]">
                    {player.name}
                  </span>

                  {/* Drop Shadow onto board */}
                  <div className="w-8 h-2 bg-black/60 rounded-full blur-xs mt-0.5"></div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Watermark in bottom corner */}
        <div className="absolute bottom-4 right-4 flex items-center gap-2 bg-slate-950/80 backdrop-blur-md px-3 py-1.5 rounded-xl border border-slate-800 pointer-events-none">
          <span className="text-[10px] text-slate-400">Cámara 3D Banquero IA</span>
          <span className="text-amber-400 font-mono text-xs font-bold">{viewAngle.toUpperCase()}</span>
        </div>
      </div>
    </div>
  );
};
