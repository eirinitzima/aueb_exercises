import React from 'react';
import { createRoot } from 'react-dom/client';
import CartPage from './components/CartPage';
import { SessionProvider } from './components/SessionContext';

const rootElement = document.getElementById('root');
const root = createRoot(rootElement);

root.render(
    <SessionProvider>
        <CartPage />
    </SessionProvider>
);
