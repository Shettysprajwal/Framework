import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { HomePage } from './pages/HomePage';
import { RegulationsListPage } from './pages/RegulationsListPage';
import { RegulationDetailPage } from './pages/RegulationDetailPage';
import { RegisterRegulationPage } from './pages/RegisterRegulationPage';
import { GraphExplorerPage } from './pages/GraphExplorerPage';
import { RuleTranslationPage } from './pages/RuleTranslationPage';
import { PolicyAdministrationPage } from './pages/PolicyAdministrationPage';
import { PipPage } from './pages/PipPage';
import { PdpPage } from './pages/PdpPage';
import { PqcCryptoPage } from './pages/PqcCryptoPage';
import { ZkProofPage } from './pages/ZkProofPage';
import { DataGovernancePage } from './pages/DataGovernancePage';
import { ComplianceMonitorPage } from './pages/ComplianceMonitorPage';
import { AuditingLedgerPage } from './pages/AuditingLedgerPage';

const App: React.FC = () => {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/regulations" element={<RegulationsListPage />} />
          <Route path="/translate" element={<RuleTranslationPage />} />
          <Route path="/policies" element={<PolicyAdministrationPage />} />
          <Route path="/pip" element={<PipPage />} />
          <Route path="/pdp" element={<PdpPage />} />
          <Route path="/pqc" element={<PqcCryptoPage />} />
          <Route path="/zkp" element={<ZkProofPage />} />
          <Route path="/governance" element={<DataGovernancePage />} />
          <Route path="/monitor" element={<ComplianceMonitorPage />} />
          <Route path="/ledger" element={<AuditingLedgerPage />} />
          <Route path="/regulations/:id" element={<RegulationDetailPage />} />
          <Route path="/register" element={<RegisterRegulationPage />} />
          <Route path="/graph" element={<GraphExplorerPage />} />
        </Routes>
      </Layout>
    </Router>
  );
};

export default App;
