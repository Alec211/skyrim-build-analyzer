import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import TournamentPage from './pages/TournamentPage'
import EncounterPage from './pages/EncounterPage'

function App() {
  return (
    <div className="app">
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/tournament" element={<TournamentPage />} />
        <Route path="/encounter" element={<EncounterPage />} />
      </Routes>
    </div>
  )
}

export default App
