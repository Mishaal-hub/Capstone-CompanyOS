import { test } from 'node:test';
import assert from 'node:assert/strict';
import { addDays, dateKey, viewDates, navigateDate, eventsForDay, toInstant, toLocalInput } from './calendarUtils.mjs';
test('month includes a complete six-week grid across year boundaries', () => {
  const dates = viewDates(new Date(2026, 0, 15), 'month');
  assert.equal(dates.length, 42); assert.equal(dateKey(dates[0]), '2025-12-28');
  assert.equal(dates[0].getDay(), 0); assert.equal(dates.at(-1).getDay(), 6);
});
test('week and day ranges use local calendar boundaries', () => {
  assert.equal(viewDates(new Date(2026, 9, 21), 'week').length, 7);
  assert.equal(dateKey(viewDates(new Date(2026, 9, 21), 'week')[0]), '2026-10-18');
  assert.equal(viewDates(new Date(), 'day').length, 1);
});
test('navigation does not overflow months and supports leap days', () => {
  assert.equal(dateKey(navigateDate(new Date(2026, 0, 31), 'month', 1)), '2026-02-01');
  assert.equal(dateKey(addDays(new Date(2028, 1, 28), 1)), '2028-02-29');
  assert.equal(dateKey(navigateDate(new Date(2026, 11, 31), 'day', 1)), '2027-01-01');
});
test('overlap includes spanning events but excludes adjacent boundaries', () => {
  const day = new Date(2026, 9, 21), start = new Date(2026, 9, 21).toISOString(), end = new Date(2026, 9, 22).toISOString();
  const events = [{id:1,startTime:new Date(2026,9,20,23).toISOString(),endTime:new Date(2026,9,21,1).toISOString()},
    {id:2,startTime:new Date(2026,9,20).toISOString(),endTime:start},
    {id:3,startTime:end,endTime:new Date(2026,9,22,1).toISOString()}];
  assert.deepEqual(eventsForDay(events, day).map(e=>e.id), [1]);
});
test('local input round trips to an unambiguous UTC timestamp', () => {
  const input='2026-10-21T09:30'; assert.equal(toLocalInput(toInstant(input)), input);
  assert.match(toInstant(input), /Z$/); assert.throws(()=>toInstant('not-a-date'));
});
test('DST gaps are rejected and local-day ranges follow clock changes', () => {
  if (Intl.DateTimeFormat().resolvedOptions().timeZone !== 'America/New_York') return;
  assert.throws(()=>toInstant('2026-03-08T02:30'));
  assert.equal((addDays(new Date(2026,2,8),1)-new Date(2026,2,8))/3600000,23);
  assert.equal((addDays(new Date(2026,10,1),1)-new Date(2026,10,1))/3600000,25);
});
