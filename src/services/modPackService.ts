import { ModPack, RoomState, BoardProperty, SurpriseCard, BillTemplate } from '../types/game';
import { DEFAULT_BILLS, DEFAULT_CARDS, DEFAULT_PROPERTIES } from '../data/defaultGameData';

// Built-in starter packs ready to play
export const BUILTIN_PACKS: ModPack[] = [
  {
    id: 'pack_fotorama_clasico',
    name: 'Fotorama México Clásico',
    author: 'Fotorama Oficial',
    version: '1.0.0',
    description: 'El clásico juego de mesa mexicano con billetes garantizados Serie A, Avenidas de CDMX y cartas de lotería.',
    currencyName: 'Pesos FM',
    currencySymbol: '$',
    playerSpins: ['🎩', '🏎️', '🚀', '👑', '🐶', '🐱', '⚽', '🍕'],
    bills: DEFAULT_BILLS,
    properties: DEFAULT_PROPERTIES,
    cards: DEFAULT_CARDS,
    isRelease: true,
    updatedAt: Date.now()
  },
  {
    id: 'pack_cyberpunk_2099',
    name: 'Imperio Cyberpunk Neo-Tokio 2099',
    author: 'Arkaios Cyber Studio',
    version: '2.0.0',
    description: 'Futuro distópico con corporaciones cibernéticas, hackeos bancarios, neo-créditos y distritos de neón.',
    currencyName: 'Neo-Créditos',
    currencySymbol: '₵',
    playerSpins: ['🤖', '🛸', '⚡', '💎', '🐉', '🥊', '🔮', '🧬'],
    bills: [
      { denomination: 500, color: 'from-cyan-600 to-blue-900', textColor: 'text-cyan-200', label: '₵500 Bit-Chip', iconName: 'Zap', signature: 'AI Arbiter', stampText: 'NEO-MINT 2099' },
      { denomination: 1000, color: 'from-fuchsia-600 to-purple-900', textColor: 'text-fuchsia-200', label: '₵1,000 Data-Cred', iconName: 'Sparkles', signature: 'AI Arbiter', stampText: 'CRYPTO-TOKEN' },
      { denomination: 5000, color: 'from-amber-500 to-rose-700', textColor: 'text-amber-100', label: '₵5,000 Quantum', iconName: 'Crown', signature: 'AI Arbiter', stampText: 'RESERVA CYBER' },
      { denomination: 20000, color: 'from-emerald-400 to-teal-800', textColor: 'text-emerald-100', label: '₵20,000 Mega-Sueldo', iconName: 'Gem', signature: 'AI Arbiter', stampText: 'SUELDO MATRIZ' }
    ],
    properties: [
      { id: 'cp_1', name: 'Callejón de Neón', group: 'Bajos Fondos', price: 800, baseRent: 90, rentPerHouse: 250, houses: 0, ownerId: null, color: '#8B4513', mortgaged: false, description: 'Mercado negro de implantes cibernéticos.' },
      { id: 'cp_2', name: 'Barrio Hacker Akihabara', group: 'Bajos Fondos', price: 1000, baseRent: 120, rentPerHouse: 300, houses: 0, ownerId: null, color: '#8B4513', mortgaged: false, description: 'Base secreta de hackers independientes.' },
      { id: 'cp_3', name: 'Distrito Shibuya Central', group: 'Tecnológico', price: 2000, baseRent: 220, rentPerHouse: 550, houses: 0, ownerId: null, color: '#06B6D4', mortgaged: false, description: 'Cruces holográficos de alta densidad publicitaria.' },
      { id: 'cp_4', name: 'Laboratorios Genéticos Biotech', group: 'Corporativo', price: 3500, baseRent: 380, rentPerHouse: 900, houses: 0, ownerId: null, color: '#EC4899', mortgaged: false, description: 'Patentes biológicas de clonación.' },
      { id: 'cp_5', name: 'Torre Megacorp Arasaka 2099', group: 'Elite', price: 8000, baseRent: 950, rentPerHouse: 2800, houses: 0, ownerId: null, color: '#1D4ED8', mortgaged: false, description: 'Rascacielos blindado que domina el skyline.' }
    ],
    cards: [
      { id: 'c_card_1', title: '¡Ataque Cibernético Exitoso!', description: 'Hackeaste la cuenta de criptomonedas del banco. ¡Cobra ₵15,000 en tu billetera fría!', type: 'RECEIVE_BANK', amount: 15000, rarity: 'legendaria', borderColor: '#06B6D4' },
      { id: 'c_card_2', title: 'Multa de la Policía Sintética', description: 'Detectaron contrabando de núcleos cuánticos sin licencia. Paga ₵4,000 a la corporación.', type: 'PAY_BANK', amount: 4000, rarity: 'trampa', borderColor: '#EF4444' },
      { id: 'c_card_3', title: 'Reinicio Forzado al Conector', description: 'Un pulso electromagnético te envía al Muro de Contención (Cárcel).', type: 'GO_TO_JAIL', rarity: 'trampa', borderColor: '#8B5CF6' }
    ],
    isRelease: true,
    updatedAt: Date.now()
  },
  {
    id: 'pack_piratas_caribe',
    name: 'Expedición Pirata Mares del Caribe',
    author: 'Capitán Barbanegra',
    version: '1.2.0',
    description: 'Navega en busca del cofre del muerto. Doblones de oro, fortalezas coloniales y maldiciones del Kraken.',
    currencyName: 'Doblones',
    currencySymbol: '⚓',
    playerSpins: ['🏴‍☠️', '🦜', '⛵', '🗡️', '🪙', '🍺', '🗺️', '🧭'],
    bills: [
      { denomination: 500, color: 'from-amber-700 to-amber-950', textColor: 'text-amber-200', label: '⚓500 Monedas de Plata', iconName: 'Coins', signature: 'Capitán Morgan', stampText: 'BOTÍN REAL' },
      { denomination: 1000, color: 'from-yellow-600 to-amber-800', textColor: 'text-yellow-100', label: '⚓1,000 Doblones de Oro', iconName: 'Gem', signature: 'Capitán Morgan', stampText: 'TESORO ESPAÑOL' },
      { denomination: 5000, color: 'from-emerald-700 to-teal-950', textColor: 'text-emerald-100', label: '⚓5,000 Rubíes de la Reina', iconName: 'Crown', signature: 'Capitán Morgan', stampText: 'CORONA REAL' }
    ],
    properties: [
      { id: 'pir_1', name: 'Bahía del Naufragio', group: 'Costas', price: 700, baseRent: 80, rentPerHouse: 220, houses: 0, ownerId: null, color: '#8B4513', mortgaged: false, description: 'Cementerio de barcos con secretos bajo el agua.' },
      { id: 'pir_2', name: 'Taverna Isla Tortuga', group: 'Piratas', price: 1800, baseRent: 190, rentPerHouse: 480, houses: 0, ownerId: null, color: '#F97316', mortgaged: false, description: 'Refugio de corsarios y mercenarios.' },
      { id: 'pir_3', name: 'Galeón Fantasma Holandés', group: 'Maldito', price: 4000, baseRent: 450, rentPerHouse: 1200, houses: 0, ownerId: null, color: '#8B5CF6', mortgaged: false, description: 'Barco legendario condenado a surcar los mares.' },
      { id: 'pir_4', name: 'Fuerte Real Port Royal', group: 'Imperio', price: 7000, baseRent: 800, rentPerHouse: 2400, houses: 0, ownerId: null, color: '#1D4ED8', mortgaged: false, description: 'El bastión más inexpugnable del Caribe.' }
    ],
    cards: [
      { id: 'pir_c_1', title: '¡Cofre del Tesoro Hundido!', description: 'Buceaste en el arrecife y sacaste un cofre colmado de oro. Cobra ⚓12,000 del banco pirata.', type: 'RECEIVE_BANK', amount: 12000, rarity: 'legendaria', borderColor: '#EAB308' },
      { id: 'pir_c_2', title: '¡Ataque del Kraken!', description: 'Los tentáculos gigantes destruyeron tus provisiones. Paga ⚓3,500 en reparaciones.', type: 'PAY_BANK', amount: 3500, rarity: 'trampa', borderColor: '#E11D48' }
    ],
    isRelease: true,
    updatedAt: Date.now()
  }
];

class ModPackService {
  private static instance: ModPackService;
  private readonly STORAGE_KEY = 'banquero_saved_modpacks';

  public static getInstance(): ModPackService {
    if (!ModPackService.instance) {
      ModPackService.instance = new ModPackService();
    }
    return ModPackService.instance;
  }

  // Get all packs (built-ins + user releases)
  getAllPacks(): ModPack[] {
    const userPacks = this.getUserPacks();
    return [...BUILTIN_PACKS, ...userPacks];
  }

  // Get only user-created packs
  getUserPacks(): ModPack[] {
    if (typeof window === 'undefined') return [];
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      return saved ? JSON.parse(saved) : [];
    } catch (err) {
      console.warn('Error reading saved modpacks:', err);
      return [];
    }
  }

  // Save or update user pack
  savePack(pack: ModPack): void {
    if (typeof window === 'undefined') return;
    const current = this.getUserPacks();
    const existingIdx = current.findIndex(p => p.id === pack.id);
    let updated: ModPack[];
    if (existingIdx >= 0) {
      updated = current.map((p, idx) => idx === existingIdx ? { ...pack, updatedAt: Date.now() } : p);
    } else {
      updated = [{ ...pack, updatedAt: Date.now() }, ...current];
    }
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(updated));
  }

  // Delete user pack
  deletePack(packId: string): void {
    if (typeof window === 'undefined') return;
    const current = this.getUserPacks().filter(p => p.id !== packId);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(current));
  }

  // Export pack as downloadable .fmpack.json file
  exportPackToFile(pack: ModPack): void {
    if (typeof window === 'undefined') return;
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(pack, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    const safeName = (pack.name || 'juego').toLowerCase().replace(/[^a-z0-9]/g, '_');
    downloadAnchor.setAttribute('download', `${safeName}_v${pack.version || '1.0.0'}.fmpack.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  }

  // Read .fmpack file from user file input
  importPackFromFile(file: File): Promise<ModPack> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const pack = JSON.parse(reader.result as string) as ModPack;
          if (!pack.name || !pack.properties || !pack.bills) {
            reject(new Error('El archivo no tiene el formato válido de un paquete de Banquero de Mesa (.fmpack).'));
            return;
          }
          if (!pack.id) pack.id = `pack_${Date.now()}`;
          this.savePack(pack);
          resolve(pack);
        } catch (err) {
          reject(err);
        }
      };
      reader.onerror = () => reject(new Error('Error al leer el archivo.'));
      reader.readAsText(file);
    });
  }

  // Apply complete pack to room state
  applyPackToRoom(pack: ModPack, currentRoom: RoomState): RoomState {
    const updatedPlayers = currentRoom.players.map((p, idx) => {
      const spins = pack.playerSpins && pack.playerSpins.length > 0 
        ? pack.playerSpins 
        : ['🎩', '🏎️', '🚀', '👑', '🐶', '🐱', '⚽', '🍕'];
      return {
        ...p,
        avatar: spins[idx % spins.length] || p.avatar,
        properties: [],
        balance: currentRoom.settings.initialBalance || 0,
        bills: {}
      };
    });

    return {
      ...currentRoom,
      properties: pack.properties.map(prop => ({ ...prop, ownerId: null })),
      customBoardImage: pack.boardImage || currentRoom.customBoardImage,
      players: updatedPlayers,
      roundNumber: 1,
      settings: {
        ...currentRoom.settings,
        currencyName: pack.currencyName || 'Pesos FM',
        currencySymbol: pack.currencySymbol || '$',
        gameStatus: 'setup'
      }
    };
  }
}

export const modPackService = ModPackService.getInstance();
