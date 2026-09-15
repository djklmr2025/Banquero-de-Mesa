import React from 'react';
import ReactDOM from 'react-dom/client';
import { CreatorStudioView } from './components/CreatorStudioView';
import { ModPack } from './types/game';
import './index.css';

export const StandaloneStudioApp: React.FC = () => {
  const handleApplyModPack = (pack: ModPack) => {
    alert(`¡Paquete "${pack.name}" aplicado! Tus billetes y propiedades han sido guardados.`);
  };

  return (
    <CreatorStudioView
      onBackToHost={() => window.location.href = '/tablet.html'}
      onApplyModPack={handleApplyModPack}
    />
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <StandaloneStudioApp />
  </React.StrictMode>
);
