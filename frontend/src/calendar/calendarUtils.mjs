export function startOfDay(value) {
  const d = new Date(value);
  return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}
export function addDays(value, count) {
  const d = new Date(value); d.setDate(d.getDate() + count); return d;
}
export function dateKey(value) {
  const d = new Date(value);
  return [d.getFullYear(), String(d.getMonth() + 1).padStart(2, '0'), String(d.getDate()).padStart(2, '0')].join('-');
}
export function viewDates(anchor, view) {
  let first = startOfDay(anchor);
  const count = view === 'month' ? 42 : view === 'week' ? 7 : 1;
  if (view === 'month') first = new Date(first.getFullYear(), first.getMonth(), 1);
  if (view !== 'day') first = addDays(first, -first.getDay());
  return Array.from({ length: count }, (_, i) => addDays(first, i));
}
export function navigateDate(anchor, view, direction) {
  if (view === 'month') return new Date(anchor.getFullYear(), anchor.getMonth() + direction, 1);
  return addDays(anchor, direction * (view === 'week' ? 7 : 1));
}
export function eventsForDay(events, day) {
  const from = startOfDay(day).getTime(), to = addDays(startOfDay(day), 1).getTime();
  return events.filter(e => Date.parse(e.startTime) < to && Date.parse(e.endTime) > from)
    .sort((a, b) => Date.parse(a.startTime) - Date.parse(b.startTime));
}
export function toLocalInput(instant) {
  const d = new Date(instant);
  return dateKey(d) + 'T' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
}
export function toInstant(localInput) {
  const date = new Date(localInput);
  if (!Number.isFinite(date.getTime()) || toLocalInput(date) !== localInput)
    throw new Error('Choose a valid local date and time (this time may not exist due to daylight saving).');
  return date.toISOString();
}
