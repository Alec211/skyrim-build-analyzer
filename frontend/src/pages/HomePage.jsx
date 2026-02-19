import { useState, useEffect } from 'react';
import { fetchDamage, fetchPerks, ARCHETYPES } from '../api/analytics';
import HorizontalBarChart from '../components/HorizontalBarChart';
import './HomePage.css';

export default function HomePage() {
  const [archetype, setArchetype] = useState('STEALTH_ARCHER');
  const [damageData, setDamageData] = useState(null);
  const [damageLoading, setDamageLoading] = useState(false);
  const [damageError, setDamageError] = useState(null);
  const [perksData, setPerksData] = useState(null);
  const [perksLoading, setPerksLoading] = useState(false);
  const [perksError, setPerksError] = useState(null);

  useEffect(() => {
    setPerksLoading(true);
    fetchPerks()
      .then(setPerksData)
      .catch(() => setPerksError('Failed to load perk data'))
      .finally(() => setPerksLoading(false));
  }, []);

  useEffect(() => {
    loadDamage();
  }, [archetype]);

  async function loadDamage() {
    setDamageLoading(true);
    setDamageError(null);
    try {
      const result = await fetchDamage(archetype);
      setDamageData(result);
    } catch {
      setDamageError('Failed to load damage data');
    }
    setDamageLoading(false);
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Skyrim Build Analyzer</h1>
        <p className="section-subtitle">All archetypes are Level 40 with 690 stat points (HP + Stamina + Magicka)</p>
      </div>

      <section>
        <div className="section-header">
          <h2>Damage Analysis</h2>
          <select value={archetype} onChange={(e) => setArchetype(e.target.value)}>
            {ARCHETYPES.map(a => <option key={a.value} value={a.value}>{a.label}</option>)}
          </select>
        </div>

        {damageLoading && <div className="loading">Loading damage data...</div>}
        {damageError && <div className="error">{damageError}</div>}

        {damageData && !damageLoading && !damageError && (
          <>
            <div className="damage-summary">
              <div className="summary-card">
                <span className="card-label">Archetype</span>
                <span className="card-value">{damageData.archetype}</span>
              </div>
              <div className="summary-card">
                <span className="card-label">Weapon</span>
                <span className="card-value">{damageData.weaponName}</span>
              </div>
              <div className="summary-card">
                <span className="card-label">Base Damage</span>
                <span className="card-value">{damageData.weaponBaseDamage}</span>
              </div>
              <div className="summary-card">
                <span className="card-label">Attack Speed</span>
                <span className="card-value">{damageData.weaponSpeed}x</span>
              </div>
              <div className="summary-card">
                <span className="card-label">Weapon DPS</span>
                <span className="card-value">{damageData.weaponDPS.toFixed(1)}</span>
              </div>
              <div className="summary-card accent">
                <span className="card-label">Perk Multiplier</span>
                <span className="card-value">{damageData.perkMultiplier.toFixed(1)}x</span>
              </div>
              <div className="summary-card accent">
                <span className="card-label">Theoretical Damage/Hit</span>
                <span className="card-value">{damageData.theoreticalDamagePerHit.toFixed(1)}</span>
              </div>
            </div>

            <div className="perk-breakdown-section">
              <h3>Perk Breakdown</h3>
              <HorizontalBarChart
                data={Object.entries(damageData.perkBreakdown).map(([name, multiplier]) => ({ name, multiplier }))}
                dataKey="multiplier"
                labelKey="name"
                tooltipFormatter={(value) => [`${value.toFixed(2)}x`, 'Multiplier']}
              />
            </div>

            {damageData.balanceWarning && (
              <div className="balance-warning">
                {damageData.balanceWarning}
              </div>
            )}
          </>
        )}
      </section>

      <section>
        <h2>Perk Efficiency</h2>

        {perksLoading && <div className="loading">Loading perk data...</div>}
        {perksError && <div className="error">{perksError}</div>}

        {perksData && !perksLoading && !perksError && (
          <>
            <div className="perk-efficiency-section">
              <h3>Damage per Perk Invested</h3>
              <p className="section-subtitle">Higher = more efficient use of perk points</p>
              <HorizontalBarChart
                data={perksData.rankings}
                dataKey="damagePerPerk"
                labelKey="archetype"
                height={350}
                labelWidth={160}
                tooltipFormatter={(value) => [`${value.toFixed(1)}`, 'Dmg/Perk']}
              />
            </div>

            <div className="perks-table-wrapper">
              <table className="perks-table">
                <thead>
                  <tr>
                    <th>Archetype</th>
                    <th>Perk Count</th>
                    <th>Perk Multiplier</th>
                    <th>Theoretical Damage</th>
                    <th>Damage / Perk</th>
                  </tr>
                </thead>
                <tbody>
                  {perksData.rankings.map(r => (
                    <tr key={r.archetype}>
                      <td className="archetype-name">{r.archetype}</td>
                      <td>{r.perkCount}</td>
                      <td>{r.perkMultiplier.toFixed(1)}x</td>
                      <td>{r.theoreticalDamage.toFixed(1)}</td>
                      <td className="efficiency-value">{r.damagePerPerk.toFixed(1)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {Object.keys(perksData.balanceWarnings).length > 0 && (
              <section>
                <h3>Balance Warnings</h3>
                {Object.entries(perksData.balanceWarnings).map(([archetype, warning]) => (
                  <div key={archetype} className="balance-warning">
                    <strong>{archetype}:</strong> {warning}
                  </div>
                ))}
              </section>
            )}
          </>
        )}
      </section>
    </div>
  );
}
