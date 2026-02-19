import { useState, useEffect } from 'react';
import { fetchEncounter, fetchEnemies, ARCHETYPES } from '../api/analytics';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import './EncounterPage.css';

export default function EncounterPage() {
  const [archetype, setArchetype] = useState('STEALTH_ARCHER');
  const [enemies, setEnemies] = useState(null);
  const [selectedEnemies, setSelectedEnemies] = useState(['Draugr', 'Draugr Wight', 'Draugr Deathlord']);
  const [fights, setFights] = useState(100);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [enemyToAdd, setEnemyToAdd] = useState('');

  useEffect(() => {
    fetchEnemies()
      .then(result => {
        const sorted = result.enemies.sort((a, b) => a.name.localeCompare(b.name));
        setEnemies(sorted);
        if (sorted.length > 0) {
          setEnemyToAdd(sorted[0].name);
        }
      })
      .catch(err => console.error('Failed to load enemies:', err));
  }, []);

  async function simulate() {
    if (selectedEnemies.length === 0) return;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchEncounter(archetype, selectedEnemies, fights);
      setData(result);
    } catch {
      setError('Failed to run encounter simulation');
    }
    setLoading(false);
  }

  function addEnemy() {
    if (enemyToAdd && selectedEnemies.length < 10) {
      setSelectedEnemies([...selectedEnemies, enemyToAdd]);
    }
  }

  function removeEnemy(index) {
    setSelectedEnemies(selectedEnemies.filter((_, i) => i !== index));
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Encounter Simulator</h1>
        <p className="section-subtitle">
          Build a wave of enemies and see how your Level 40 archetype survives. HP carries over between fights.
        </p>
      </div>

      <div className="encounter-setup">
        <div className="encounter-archetype">
          <label>Player Archetype:</label>
          <select value={archetype} onChange={(e) => setArchetype(e.target.value)}>
            {ARCHETYPES.map(a => <option key={a.value} value={a.value}>{a.label}</option>)}
          </select>
        </div>

        <div className="encounter-wave">
          <label>Enemy Wave:</label>
          <div className="wave-list">
            {selectedEnemies.map((enemy, i) => (
              <div key={i} className="wave-enemy">
                <span className="wave-number">{i + 1}.</span>
                <span className="wave-name">{enemy}</span>
                <button className="wave-remove" onClick={() => removeEnemy(i)}>x</button>
              </div>
            ))}
          </div>
          {enemies && selectedEnemies.length < 10 && (
            <div className="wave-add">
              <select value={enemyToAdd} onChange={(e) => setEnemyToAdd(e.target.value)}>
                {enemies.map(e => (
                  <option key={e.name} value={e.name}>
                    {e.name} (Lvl {e.level}, HP {e.health})
                  </option>
                ))}
              </select>
              <button onClick={addEnemy}>Add</button>
            </div>
          )}
        </div>

        <div className="encounter-fights">
          <label>
            Simulation runs:
            <input
              type="range"
              min={10}
              max={500}
              step={10}
              value={fights}
              onChange={(e) => setFights(Number(e.target.value))}
            />
            <span className="fights-value">{fights}</span>
          </label>
        </div>

        <button className="simulate-btn" onClick={simulate} disabled={loading || selectedEnemies.length === 0}>
          {loading ? 'Simulating...' : 'Run Encounter'}
        </button>
      </div>

      {loading && <div className="loading">Simulating {fights} encounter runs...</div>}
      {error && <div className="error">{error}</div>}

      {data && !loading && !error && (
        <>
          <section className="encounter-results">
            <h2>Results</h2>
            <div className="encounter-stats-grid">
              <div className="encounter-stat">
                <span className="stat-label">Survival Rate</span>
                <span className="stat-value big" style={{ color: data.survivalRate >= 50 ? '#6b8e4e' : '#a04545' }}>
                  {data.survivalRate.toFixed(1)}%
                </span>
              </div>
              <div className="encounter-stat">
                <span className="stat-label">Avg Enemies Defeated</span>
                <span className="stat-value big">{data.avgEnemiesDefeated.toFixed(1)} / {selectedEnemies.length}</span>
              </div>
              <div className="encounter-stat">
                <span className="stat-label">Avg Damage Dealt</span>
                <span className="stat-value">{data.avgDamageDealt.toFixed(0)}</span>
              </div>
              <div className="encounter-stat">
                <span className="stat-label">Avg Damage Received</span>
                <span className="stat-value">{data.avgDamageReceived.toFixed(0)}</span>
              </div>
            </div>
          </section>

          <section>
            <h2>Per-Enemy Breakdown</h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={data.perEnemyBreakdown} layout="vertical" margin={{ left: 120, right: 20, top: 10, bottom: 10 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                <XAxis type="number" domain={[0, 100]} tick={{ fill: '#888' }} />
                <YAxis type="category" dataKey="enemyName" tick={{ fill: '#c8c8c8' }} width={110} />
                <Tooltip
                  contentStyle={{ background: '#1a1a1a', border: '1px solid #3a3a38' }}
                  labelStyle={{ color: '#d5d0c4' }}
                />
                <Bar dataKey="winRate" name="Win Rate %" fill="#9a968c" />
              </BarChart>
            </ResponsiveContainer>
          </section>

          <section>
            <h2>Damage Per Enemy</h2>
            <div className="perks-table-wrapper">
              <table className="perks-table">
                <thead>
                  <tr>
                    <th>Enemy</th>
                    <th>Win Rate</th>
                    <th>Avg Damage Dealt</th>
                    <th>Avg Damage Received</th>
                  </tr>
                </thead>
                <tbody>
                  {data.perEnemyBreakdown.map(e => (
                    <tr key={e.enemyName}>
                      <td>{e.enemyName}</td>
                      <td style={{ color: e.winRate >= 50 ? '#6b8e4e' : '#a04545' }}>{e.winRate.toFixed(1)}%</td>
                      <td>{e.avgDamageDealt.toFixed(1)}</td>
                      <td>{e.avgDamageReceived.toFixed(1)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}
    </div>
  );
}
