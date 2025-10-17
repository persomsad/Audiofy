/**
 * UUID 工具函数
 * 提供 NativeScript 兼容的 UUID v4 生成器
 */

/**
 * 生成 UUID v4
 * @returns 标准格式的 UUID 字符串（例如：550e8400-e29b-41d4-a716-446655440000）
 *
 * @example
 * const id = generateUUID()
 * console.log(id) // '550e8400-e29b-41d4-a716-446655440000'
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 验证 UUID 格式
 * @param uuid 待验证的 UUID 字符串
 * @returns 是否为有效的 UUID v4 格式
 *
 * @example
 * isValidUUID('550e8400-e29b-41d4-a716-446655440000') // true
 * isValidUUID('invalid-uuid') // false
 */
export function isValidUUID(uuid: string): boolean {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
  return uuidRegex.test(uuid)
}
