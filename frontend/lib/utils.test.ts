import { describe, expect, test } from "vitest";
import { formatMinutes, getOwnershipColor } from "./utils";

describe("formatMinutes", () => {
  test("less than 1 minute", () => {
    expect(formatMinutes(29)).toBe("<1 мин");
  });

  test("exactly one minute", () => {
    expect(formatMinutes(60)).toBe("1 мин");
  });

  test("rounds to one minute", () => {
    expect(formatMinutes(89)).toBe("1 мин");
  });

  test("plural minutes", () => {
    expect(formatMinutes(120)).toBe("2 мин");
  });

  test("rounds up to two minutes", () => {
    expect(formatMinutes(90)).toBe("2 мин");
  });

  test("zero seconds", () => {
    expect(formatMinutes(0)).toBe("<1 мин");
  });
});

describe("getOwnershipColor", () => {
  test("municipal is blue", () => {
    expect(getOwnershipColor("MUNICIPAL")).toBe("#2563eb");
  });

  test("private SRZI is green", () => {
    expect(getOwnershipColor("PRIVATE_SRZI")).toBe("#059669");
  });

  test("private MON is orange", () => {
    expect(getOwnershipColor("PRIVATE_MON")).toBe("#fc9003");
  });

  test("unknown type falls back to orange", () => {
    expect(getOwnershipColor("UNKNOWN")).toBe("#fc9003");
  });
});
