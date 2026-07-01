import { describe, expect, test } from "vitest";
import { haversineDistance } from "./haversine";

describe("haversineDistance", () => {
  test("same point returns zero", () => {
    expect(haversineDistance(42.6977, 23.3219, 42.6977, 23.3219)).toBe(0);
  });

  test("symmetric", () => {
    const a = haversineDistance(42.6977, 23.3219, 42.7, 23.33);
    const b = haversineDistance(42.7, 23.33, 42.6977, 23.3219);
    expect(a).toBeCloseTo(b, 6);
  });

  test("Sofia landmarks: NDK to Serdika ~1.4 km", () => {
    const distance = haversineDistance(42.6977, 23.3219, 42.6978, 23.3228);
    expect(distance).toBeGreaterThan(70);
    expect(distance).toBeLessThan(150);
  });

  test("Sofia to Plovdiv ~130 km", () => {
    const distance = haversineDistance(42.6977, 23.3219, 42.1354, 24.7453);
    expect(distance).toBeGreaterThan(120_000);
    expect(distance).toBeLessThan(140_000);
  });

  test("poles", () => {
    const distance = haversineDistance(90, 0, -90, 0);
    expect(distance).toBeGreaterThan(20_000_000);
  });
});
