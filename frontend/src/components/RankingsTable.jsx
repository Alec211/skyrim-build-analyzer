import TierBadge from './TierBadge';
import './RankingsTable.css';

export default function RankingsTable({ rankings }) {
  if (!rankings || rankings.length === 0) return null;

  return (
    <div className="rankings-table-wrapper">
      <table className="rankings-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Fighter</th>
            <th>Tier</th>
            <th>Win Rate</th>
            <th>W</th>
            <th>L</th>
            <th>D</th>
            <th>Best Matchup</th>
            <th>Worst Matchup</th>
          </tr>
        </thead>
        <tbody>
          {rankings.map((r, i) => (
            <tr key={r.fighterName}>
              <td>{i + 1}</td>
              <td className="archetype-name">{r.fighterName}</td>
              <td><TierBadge tier={r.tier} /></td>
              <td>
                <div className="win-rate-cell">
                  <div className="win-rate-bar" style={{ width: `${r.overallWinRate}%` }} />
                  <span>{r.overallWinRate.toFixed(1)}%</span>
                </div>
              </td>
              <td className="stat-win">{r.totalWins}</td>
              <td className="stat-loss">{r.totalLosses}</td>
              <td className="stat-draw">{r.totalDraws}</td>
              <td>{r.bestMatchup}</td>
              <td>{r.worstMatchup}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
