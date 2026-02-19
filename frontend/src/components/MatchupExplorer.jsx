import { useState } from 'react';
import './MatchupExplorer.css';

export default function MatchupExplorer({ matrix, names }) {
  const [selected, setSelected] = useState(0);

  if (!matrix || !names || names.length === 0) return null;

  const matchups = names
    .map((name, i) => ({
      opponent: name,
      winRate: matrix[selected][i],
      isSelf: i === selected,
    }))
    .filter(m => !m.isSelf)
    .sort((a, b) => b.winRate - a.winRate);

  return (
    <div className="matchup-explorer">
      <div className="explorer-select">
        <label>View matchups for:</label>
        <select value={selected} onChange={(e) => setSelected(Number(e.target.value))}>
          {names.map((name, i) => (
            <option key={name} value={i}>{name}</option>
          ))}
        </select>
      </div>

      <div className="explorer-list">
        {matchups.map(m => (
          <div key={m.opponent} className="explorer-row">
            <span className="explorer-opponent">{m.opponent}</span>
            <div className="explorer-bar-track">
              <div
                className={`explorer-bar-fill ${m.winRate >= 50 ? 'win' : 'loss'}`}
                style={{ width: `${m.winRate}%` }}
              />
              <span className="explorer-bar-label">{m.winRate.toFixed(0)}%</span>
            </div>
            <span className={`explorer-result ${m.winRate >= 50 ? 'win' : 'loss'}`}>
              {m.winRate >= 70 ? 'Dominant' : m.winRate >= 50 ? 'Favored' : m.winRate >= 30 ? 'Unfavored' : 'Dominated'}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
