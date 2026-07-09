import { describe, expect, test } from "bun:test";
import { normalizeSparklinePoints } from "./SparklineUtils";

describe("normalizeSparklinePoints", () => {
  test("returns empty string for empty array", () => {
    expect(normalizeSparklinePoints([], 100, 40)).toBe("");
  });

  test("maps single point to vertical center", () => {
    const result = normalizeSparklinePoints([5], 100, 40, 2);
    expect(result).toBe("2,20");
  });

  test("normalizes min to bottom and max to top", () => {
    const result = normalizeSparklinePoints([0, 10], 100, 40, 2);
    const parts = result.split(" ");
    expect(parts).toHaveLength(2);
    // first point should be at bottom (higher y value)
    // second point should be at top (lower y value)
    const [, y1] = parts[0].split(",").map(Number);
    const [, y2] = parts[1].split(",").map(Number);
    expect(y1).toBeGreaterThan(y2);
  });

  test("spreads points evenly across width", () => {
    const result = normalizeSparklinePoints([1, 2, 3, 4, 5], 100, 40, 2);
    const parts = result.split(" ");
    expect(parts).toHaveLength(5);
    const xValues = parts.map((p) => Number(p.split(",")[0]));
    expect(xValues[0]).toBe(2);
    expect(xValues[xValues.length - 1]).toBe(98);
  });

  test("handles flat data without division by zero", () => {
    const result = normalizeSparklinePoints([5, 5, 5], 100, 40, 2);
    const parts = result.split(" ");
    expect(parts).toHaveLength(3);
    const yValues = parts.map((p) => Number(p.split(",")[1]));
    // all y should be the same (midpoint)
    expect(new Set(yValues).size).toBe(1);
  });
});
