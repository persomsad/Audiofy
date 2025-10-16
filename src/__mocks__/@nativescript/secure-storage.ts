// src/__mocks__/@nativescript/secure-storage.ts
import { vi } from 'vitest'

export const SecureStorage = vi.fn().mockImplementation(() => ({
  get: vi.fn((key: string) => Promise.resolve(`mocked-secret-for-${key}`)),
  set: vi.fn(() => Promise.resolve(true)),
  remove: vi.fn(() => Promise.resolve(true)),
  clear: vi.fn(() => Promise.resolve(true)),
}))
