import { BillTemplate, BoardProperty, SurpriseCard } from '../types/game';

export const DEFAULT_BILLS: BillTemplate[] = [
  {
    denomination: 500,
    color: 'from-amber-600 to-amber-700',
    textColor: 'text-amber-100',
    label: '$500 Quinientos',
    iconName: 'Coins',
    signature: 'Banquero FM',
    stampText: 'FOTORAMA OFICIAL'
  },
  {
    denomination: 1000,
    color: 'from-emerald-600 to-emerald-800',
    textColor: 'text-emerald-100',
    label: '$1,000 Mil Pesos',
    iconName: 'DollarSign',
    signature: 'Banquero FM',
    stampText: 'SERIE A - GARANTIZADO'
  },
  {
    denomination: 2000,
    color: 'from-cyan-600 to-blue-800',
    textColor: 'text-cyan-100',
    label: '$2,000 Dos Mil',
    iconName: 'Zap',
    signature: 'Banquero FM',
    stampText: 'BANCO DIGITAL FM'
  },
  {
    denomination: 5000,
    color: 'from-purple-600 to-indigo-800',
    textColor: 'text-purple-100',
    label: '$5,000 Cinco Mil',
    iconName: 'Sparkles',
    signature: 'Banquero FM',
    stampText: 'RESERVA FEDERAL FM'
  },
  {
    denomination: 10000,
    color: 'from-rose-600 to-pink-800',
    textColor: 'text-rose-100',
    label: '$10,000 Diez Mil',
    iconName: 'Crown',
    signature: 'Banquero FM',
    stampText: 'ORO FOTORAMA'
  },
  {
    denomination: 20000,
    color: 'from-yellow-500 to-amber-600',
    textColor: 'text-amber-950',
    label: '$20,000 Veinte Mil',
    iconName: 'Gem',
    signature: 'Banquero FM',
    stampText: 'SUELDO DE SALIDA'
  },
  {
    denomination: 50000,
    color: 'from-slate-800 to-slate-950 border-amber-400',
    textColor: 'text-amber-300',
    label: '$50,000 Cincuenta Mil Platinum',
    iconName: 'ShieldAlert',
    signature: 'Banquero FM',
    stampText: 'BILLETE DIAMANTE'
  }
];

export const DEFAULT_PROPERTIES: BoardProperty[] = [
  {
    id: 'prop_1',
    name: 'Avenida Cuauhtémoc',
    group: 'Marrón',
    price: 600,
    baseRent: 50,
    rentPerHouse: 150,
    houses: 0,
    ownerId: null,
    color: '#8B4513',
    mortgaged: false
  },
  {
    id: 'prop_2',
    name: 'Calzada de Tlalpan',
    group: 'Marrón',
    price: 800,
    baseRent: 80,
    rentPerHouse: 200,
    houses: 0,
    ownerId: null,
    color: '#8B4513',
    mortgaged: false
  },
  {
    id: 'prop_3',
    name: 'Paseo de la Reforma',
    group: 'Azul Cielo',
    price: 1500,
    baseRent: 150,
    rentPerHouse: 400,
    houses: 0,
    ownerId: null,
    color: '#38BDF8',
    mortgaged: false
  },
  {
    id: 'prop_4',
    name: 'Avenida Insurgentes',
    group: 'Azul Cielo',
    price: 1800,
    baseRent: 180,
    rentPerHouse: 450,
    houses: 0,
    ownerId: null,
    color: '#38BDF8',
    mortgaged: false
  },
  {
    id: 'prop_5',
    name: 'Zona Hotelera Cancún',
    group: 'Magenta',
    price: 2600,
    baseRent: 260,
    rentPerHouse: 700,
    houses: 0,
    ownerId: null,
    color: '#D946EF',
    mortgaged: false
  },
  {
    id: 'prop_6',
    name: 'Playa del Carmen',
    group: 'Magenta',
    price: 3000,
    baseRent: 320,
    rentPerHouse: 800,
    houses: 0,
    ownerId: null,
    color: '#D946EF',
    mortgaged: false
  },
  {
    id: 'prop_7',
    name: 'Polanco Residencial',
    group: 'Naranja',
    price: 3800,
    baseRent: 400,
    rentPerHouse: 1000,
    houses: 0,
    ownerId: null,
    color: '#F97316',
    mortgaged: false
  },
  {
    id: 'prop_8',
    name: 'Bosques de las Lomas',
    group: 'Naranja',
    price: 4200,
    baseRent: 450,
    rentPerHouse: 1200,
    houses: 0,
    ownerId: null,
    color: '#F97316',
    mortgaged: false
  },
  {
    id: 'prop_9',
    name: 'Los Cabos Luxury Resort',
    group: 'Azul Marino',
    price: 6000,
    baseRent: 650,
    rentPerHouse: 2000,
    houses: 0,
    ownerId: null,
    color: '#1D4ED8',
    mortgaged: false
  },
  {
    id: 'prop_10',
    name: 'Punta Mita Exclusivo',
    group: 'Azul Marino',
    price: 7500,
    baseRent: 850,
    rentPerHouse: 2500,
    houses: 0,
    ownerId: null,
    color: '#1D4ED8',
    mortgaged: false
  }
];

export const DEFAULT_CARDS: SurpriseCard[] = [
  {
    id: 'card_1',
    title: '¡Premio de Lotería!',
    description: 'El banco de Fotorama te entrega tu premio gordo. ¡Cobra en ventanilla!',
    type: 'RECEIVE_BANK',
    amount: 10000,
    rarity: 'legendaria'
  },
  {
    id: 'card_2',
    title: 'Multa por Exceso de Velocidad',
    description: 'Ibas a 120 km/h en carril de baja. Paga la infracción a la tesorería del banco.',
    type: 'PAY_BANK',
    amount: 3000,
    rarity: 'comun'
  },
  {
    id: 'card_3',
    title: '¡Es tu Cumpleaños!',
    description: 'Todos los jugadores en la mesa te regalan $1,000 por tu fiesta.',
    type: 'RECEIVE_FROM_PLAYERS',
    amount: 1000,
    rarity: 'rara'
  },
  {
    id: 'card_4',
    title: '¡A la Cárcel sin Escalas!',
    description: 'Te pillaron en negocios turbios. Ve directo al calabozo sin cobrar sueldo.',
    type: 'GO_TO_JAIL',
    rarity: 'trampa'
  },
  {
    id: 'card_5',
    title: 'Ronda de Tacos para Todos',
    description: 'Te sentiste generoso y pagaste la cena de todos los jugadores ($1,500 cada uno).',
    type: 'PAY_PLAYERS',
    amount: 1500,
    rarity: 'rara'
  },
  {
    id: 'card_6',
    title: 'Vuelo Express a Cancún',
    description: 'Toma un vuelo charter directo a Zona Hotelera Cancún. Si no tiene dueño, la compras.',
    type: 'MOVE_TO',
    targetPosition: 4,
    rarity: 'comun'
  }
];
