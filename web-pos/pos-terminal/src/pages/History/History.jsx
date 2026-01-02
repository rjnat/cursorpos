import { useTranslation } from 'react-i18next';

function History() {
  const { t } = useTranslation();

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-4">
        {t('pos.history')}
      </h1>
      <p className="text-gray-600">Transaction history - Coming soon</p>
    </div>
  );
}

export default History;
