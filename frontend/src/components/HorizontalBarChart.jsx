import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const COLORS = ['#d5d0c4', '#9a968c', '#6b8e4e', '#5a7a9e', '#8a8680', '#b8b4aa', '#5a6a4a', '#6a7a8a'];

export default function HorizontalBarChart({ data, dataKey, labelKey, height = 300, labelWidth = 140, tooltipFormatter }) {
  if (!data || data.length === 0) return null;

  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={data} layout="vertical" margin={{ left: 20, right: 20 }}>
        <XAxis type="number" stroke="#888" fontSize={12} />
        <YAxis type="category" dataKey={labelKey} stroke="#888" fontSize={12} width={labelWidth} />
        <Tooltip
          contentStyle={{ background: '#1a1a1a', border: '1px solid #3a3a38' }}
          labelStyle={{ color: '#d5d0c4' }}
          itemStyle={{ color: '#d5d0c4' }}
          formatter={tooltipFormatter}
        />
        <Bar dataKey={dataKey} radius={[0, 2, 2, 0]}>
          {data.map((_, i) => (
            <Cell key={i} fill={COLORS[i % COLORS.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}
