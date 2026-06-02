import { describe, expect, test } from "bun:test";
import { resolveRiskColor } from "./RiskPillUtils";

describe("resolveRiskColor", () => {
  test("maps 高 to danger color", () => {
    expect(resolveRiskColor("高")).toBe("var(--imld-risk-high)");
  });
  test("maps 中 to warning color", () => {
    expect(resolveRiskColor("中")).toBe("var(--imld-risk-mid)");
  });
  test("maps 低 to success color", () => {
    expect(resolveRiskColor("低")).toBe("var(--imld-risk-low)");
  });
  test("defaults to low for unknown", () => {
    expect(resolveRiskColor("未知")).toBe("var(--imld-risk-low)");
  });
});
