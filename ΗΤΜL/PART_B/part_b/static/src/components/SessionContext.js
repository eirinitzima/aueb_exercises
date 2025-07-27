import React, { createContext, useContext, useEffect, useState } from 'react';

const SessionContext = createContext();

export const SessionProvider = ({ children }) => {
    const [session, setSession] = useState(() => {
        const username = localStorage.getItem('username');
        const sessionId = localStorage.getItem('sessionId');
        return username && sessionId ? { username, sessionId } : null;
    });

    const logOut = () => {
        
        const cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];
    
      
        localStorage.removeItem('username');
        localStorage.removeItem('sessionId');
        setSession(null);
    
      
        console.log('User logged out. Cart items retained:', cartItems);
    };
    

    useEffect(() => {
        if (!session?.username || !session?.sessionId) {
            console.warn('Session is not set properly. Ensure user is logged in.');
        }
    }, [session]);

    return (
        <SessionContext.Provider value={{ session, setSession, logOut }}>
            {children}
        </SessionContext.Provider>
    );
};

export const useSession = () => useContext(SessionContext);
