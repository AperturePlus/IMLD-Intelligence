export interface ForecastPoint {
  x: string
  y: number
}

export interface ForecastChartProps {
  history: ForecastPoint[]
  forecast: ForecastPoint[]
}
