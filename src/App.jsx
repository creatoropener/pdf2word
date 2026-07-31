import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import { ToastProvider } from './hooks/useToast';
import { DarkModeProvider } from './hooks/useDarkMode';
import { RecentConversionsProvider } from './hooks/useRecentConversions';
import { SettingsProvider } from './hooks/useSettings';

export default function App() {
  return (
    <DarkModeProvider>
      <ToastProvider>
        <SettingsProvider>
          <RecentConversionsProvider>
            <Layout>
              <HomePage />
            </Layout>
          </RecentConversionsProvider>
        </SettingsProvider>
      </ToastProvider>
    </DarkModeProvider>
  );
}