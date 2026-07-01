import { describe, expect, test, beforeAll, afterEach, afterAll } from "vitest";
import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import { searchFacilities } from "./api-client";

const server = setupServer();

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe("searchFacilities", () => {
  test("returns facilities on success", async () => {
    server.use(
      http.get("http://localhost:8080/api/facilities", () => {
        return HttpResponse.json([{ id: "1", name: "ДГ №1" }]);
      })
    );

    const result = await searchFacilities();
    expect(result).toEqual([{ id: "1", name: "ДГ №1" }]);
  });

  test("throws on HTTP error", async () => {
    server.use(
      http.get("http://localhost:8080/api/facilities", () => {
        return new HttpResponse(null, { status: 500 });
      })
    );

    await expect(searchFacilities()).rejects.toThrow("Search failed: 500");
  });

  test("throws on network error", async () => {
    server.use(
      http.get("http://localhost:8080/api/facilities", () => {
        return HttpResponse.error();
      })
    );

    await expect(searchFacilities()).rejects.toThrow();
  });
});
