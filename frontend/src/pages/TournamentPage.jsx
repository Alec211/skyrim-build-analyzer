import { useState, useEffect } from 'react';
import { fetchTournament } from '../api/analytics';
import RankingsTable from '../components/RankingsTable';
import MatchupExplorer from '../components/MatchupExplorer';
import './TournamentPage.css';

export default function TournamentPage() {
  const [data, setData] = useState(null);
  const [fights, setFights] = useState(100);
  const [includeEnemies, setIncludeEnemies] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadTournament();
  }, []);

  async function loadTournament() {
    setLoading(true);
    setError(null);
    try {
      const result = await fetchTournament(fights, includeEnemies);
      setData(result);
    } catch {
      setError('Failed to run tournament simulation');
    }
    setLoading(false);
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Tournament Rankings</h1>
        <div className="controls">
          <label>
            Fights per matchup:
            <input
              type="range"
              min={50}
              max={500}
              step={50}
              value={fights}
              onChange={(e) => setFights(Number(e.target.value))}
            />
            <span className="fights-value">{fights}</span>
          </label>
          <label className="toggle-label">
            <input
              type="checkbox"
              checked={includeEnemies}
              onChange={(e) => setIncludeEnemies(e.target.checked)}
            />
            Include Enemies
          </label>
          <button onClick={loadTournament} disabled={loading}>
            {loading ? 'Simulating...' : 'Run Tournament'}
          </button>
        </div>
      </div>

      {loading && <div className="loading">Running {fights} fights per matchup across all fighters...</div>}
      {error && <div className="error">{error}</div>}

      {data && !loading && !error && (
        <>
          <section>
            <h2>Tier List</h2>
            <RankingsTable rankings={data.rankings} />
          </section>

          <section>
            <h2>Matchup Explorer</h2>
            <p className="section-subtitle">Select a fighter to see their win rates against every opponent</p>
            <MatchupExplorer matrix={data.matchupMatrix} names={data.archetypeNames} />
          </section>
        </>
      )}
    </div>
  );
}
