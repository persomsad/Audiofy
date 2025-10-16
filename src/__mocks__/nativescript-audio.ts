/**
 * nativescript-audio Mock
 * 用于测试环境
 */

export class TNSPlayer {
  initFromFile: (...args: any[]) => Promise<any>
  play: () => Promise<void>
  pause: () => Promise<void>
  seekTo: (ms: number) => Promise<void>
  dispose: () => Promise<void>
  getAudioTrackDuration: () => Promise<number>
  currentTime: number

  constructor() {
    this.initFromFile = vi.fn().mockResolvedValue(undefined)
    this.play = vi.fn().mockResolvedValue(undefined)
    this.pause = vi.fn().mockResolvedValue(undefined)
    this.seekTo = vi.fn().mockResolvedValue(undefined)
    this.dispose = vi.fn().mockResolvedValue(undefined)
    this.getAudioTrackDuration = vi.fn().mockResolvedValue(10000)
    this.currentTime = 0
  }
}

export class TNSRecorder {
  constructor() {
    // Mock recorder (not used in current implementation)
  }
}
