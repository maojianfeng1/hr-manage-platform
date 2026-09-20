/** 2026-09-16T11:40:35 → 2026-09-16 11:40:35 */
export function fmtDateTime(v) {
  if (!v) return ''
  return String(v).replace('T', ' ').slice(0, 19)
}

/** 2026-09-16 → 2026-09-16（日期型字段用） */
export function fmtDate(v) {
  if (!v) return ''
  return String(v).slice(0, 10)
}

