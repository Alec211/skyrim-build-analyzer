import './TierBadge.css';

const TIER_COLORS = {
  S: '#e0dcd2',
  A: '#b8b4aa',
  B: '#6b8e4e',
  C: '#5a7a9e',
  D: '#555555',
};

export default function TierBadge({ tier }) {
  return (
    <span className="tier-badge" style={{ background: TIER_COLORS[tier] || '#444' }}>
      {tier}
    </span>
  );
}
