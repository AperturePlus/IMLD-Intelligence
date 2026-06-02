import { describe, expect, test } from "bun:test";
import { splitTextChunks } from "./StreamingTextUtils";

describe("splitTextChunks", () => {
  test("splits ASCII text into chars", () => {
    expect(splitTextChunks("hello")).toEqual(["h", "e", "l", "l", "o"]);
  });

  test("splits Chinese text into chars", () => {
    expect(splitTextChunks("你好")).toEqual(["你", "好"]);
  });

  test("handles empty string", () => {
    expect(splitTextChunks("")).toEqual([]);
  });

  test("preserves emoji as single chunks", () => {
    const chunks = splitTextChunks("a👋b");
    expect(chunks).toEqual(["a", "👋", "b"]);
  });
});
