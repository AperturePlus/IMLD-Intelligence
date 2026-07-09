export const normalizeSparklinePoints = (
  values: number[],
  w: number,
  h: number,
  padding = 2
): string => {
  if (values.length === 0) return "";
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;
  const step = values.length > 1 ? (w - padding * 2) / (values.length - 1) : 0;
  const midY = padding + (h - padding * 2) / 2;

  return values
    .map((v, i) => {
      const x = padding + i * step;
      const y =
        max === min
          ? midY
          : padding + (1 - (v - min) / range) * (h - padding * 2);
      return `${x},${y}`;
    })
    .join(" ");
};
