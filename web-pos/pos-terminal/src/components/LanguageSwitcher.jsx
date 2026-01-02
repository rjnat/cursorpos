import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { LanguageIcon } from '@heroicons/react/24/outline';

/**
 * LanguageSwitcher Component
 * Allows users to switch between English and Indonesian
 * Persists language preference in localStorage
 */
const LanguageSwitcher = () => {
  const { i18n } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);

  const languages = [
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'id', name: 'Indonesia', flag: '🇮🇩' },
  ];

  const currentLanguage = languages.find(lang => lang.code === i18n.language) || languages[0];

  const handleLanguageChange = (langCode) => {
    i18n.changeLanguage(langCode);
    localStorage.setItem('language', langCode);
    document.documentElement.lang = langCode;
    setIsOpen(false);
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 py-2 rounded-lg
                 text-gray-700 hover:bg-gray-100 transition-colors"
        aria-label="Change language"
      >
        <LanguageIcon className="h-5 w-5" />
        <span className="text-xl">{currentLanguage.flag}</span>
        <span className="hidden md:inline text-sm font-medium">
          {currentLanguage.code.toUpperCase()}
        </span>
      </button>

      {isOpen && (
        <>
          <div
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
          />
          <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
            <div className="py-1">
              {languages.map((lang) => (
                <button
                  key={lang.code}
                  onClick={() => handleLanguageChange(lang.code)}
                  className={`w-full px-4 py-2 text-left flex items-center gap-3
                           hover:bg-gray-50 transition-colors
                           ${lang.code === i18n.language ? 'bg-blue-50 text-blue-600' : 'text-gray-700'}`}
                >
                  <span className="text-xl">{lang.flag}</span>
                  <div>
                    <div className="font-medium">{lang.name}</div>
                    <div className="text-xs text-gray-500">{lang.code.toUpperCase()}</div>
                  </div>
                  {lang.code === i18n.language && (
                    <svg
                      className="ml-auto h-5 w-5 text-blue-600"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                        clipRule="evenodd"
                      />
                    </svg>
                  )}
                </button>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default LanguageSwitcher;
