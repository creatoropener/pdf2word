import { createContext, useContext, useState, useCallback, useEffect } from 'react';

const RecentContext = createContext();

export function RecentConversionsProvider({ children }) {
  const [recent, setRecent] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('recent-conversions') || '[]');
    } catch {
      return [];
    }
  });

  useEffect(() => {
    localStorage.setItem('recent-conversions', JSON.stringify(recent));
  }, [recent]);

  const addEntry = useCallback((entry) => {
    setRecent((prev) => [entry, ...prev].slice(0, 20));
  }, []);

  return (
    <RecentContext.Provider value={{ recent, addEntry }}>
      {children}
    </RecentContext.Provider>
  );
}

export const useRecentConversions = () => useContext(RecentContext);