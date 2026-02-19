const BASE = '';

export async function fetchTournament(fights = 100, includeEnemies = false) {
  const res = await fetch(`${BASE}/analytics/tournament?fights=${fights}&includeEnemies=${includeEnemies}`);
  if (!res.ok) throw new Error('Failed to fetch tournament');
  return res.json();
}

export async function fetchDamage(archetype = 'STEALTH_ARCHER') {
  const res = await fetch(`${BASE}/analytics/damage?archetype=${archetype}`);
  if (!res.ok) throw new Error('Failed to fetch damage');
  return res.json();
}

export async function fetchPerks() {
  const res = await fetch(`${BASE}/analytics/perks`);
  if (!res.ok) throw new Error('Failed to fetch perks');
  return res.json();
}

export async function fetchEnemies(category) {
  const url = category
    ? `${BASE}/analytics/enemies?category=${category}`
    : `${BASE}/analytics/enemies`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch enemies');
  return res.json();
}

export async function fetchEncounter(archetype, enemies, fights = 100) {
  const enemyParam = enemies.map(e => encodeURIComponent(e)).join(',');
  const res = await fetch(`${BASE}/analytics/encounter?archetype=${archetype}&enemies=${enemyParam}&fights=${fights}`);
  if (!res.ok) throw new Error('Failed to fetch encounter');
  return res.json();
}

export const ARCHETYPES = [
  { value: 'STEALTH_ARCHER', label: 'Stealth Archer' },
  { value: 'TWO_HANDED_WARRIOR', label: 'Two-Handed Warrior' },
  { value: 'DUAL_WIELDING_BERSERKER', label: 'Dual-Wielding Berserker' },
  { value: 'ASSASSIN', label: 'Assassin' },
  { value: 'PALADIN', label: 'Paladin' },
  { value: 'BARBARIAN', label: 'Barbarian' },
  { value: 'RANGER', label: 'Ranger' },
  { value: 'SPELLSWORD', label: 'Spellsword' },
];
