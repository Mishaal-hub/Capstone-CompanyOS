import { useEffect, useRef, useState } from 'react';
import { getCalendarEvents, createCalendarEvent, updateCalendarEvent, deleteCalendarEvent } from '../api';
import { addDays, dateKey, eventsForDay, navigateDate, toInstant, toLocalInput, viewDates } from '../calendar/calendarUtils.mjs';
import './Calendar.css';

const zone = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
const categories = ['MEETING', 'DEADLINE', 'FOCUS', 'OTHER'];
const statuses = ['SCHEDULED', 'COMPLETED', 'CANCELLED'];
const label = value => value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();

function EventDialog({ draft, onClose, onSaved }) {
  const dialog = useRef(null);
  const titleInput = useRef(null);
  const [form, setForm] = useState(draft);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => { const node = dialog.current; node.showModal(); titleInput.current?.focus(); return () => node.close(); }, []);
  const field = name => ({ value: form[name], onChange: e => setForm({ ...form, [name]: e.target.value }) });

  async function save(e) {
    e.preventDefault(); setError('');
    try {
      const startTime = toInstant(form.start), endTime = toInstant(form.end);
      if (Date.parse(endTime) <= Date.parse(startTime)) throw new Error('End must be after start.');
      const data = { title: form.title.trim(), description: form.description, startTime, endTime,
        timeZone: zone, eventType: form.eventType, status: form.status, location: form.location };
      setBusy(true);
      if (form.id) await updateCalendarEvent(form.id, data); else await createCalendarEvent(data);
      onSaved();
    } catch (err) { setError(err.message); setBusy(false); }
  }
  async function remove() {
    if (!window.confirm('Delete "' + form.title + '"? This cannot be undone.')) return;
    setBusy(true); setError('');
    try { await deleteCalendarEvent(form.id); onSaved(); }
    catch (err) { setError(err.message); setBusy(false); }
  }
  return <dialog ref={dialog} className="calendar-dialog" aria-labelledby="event-heading"
    onCancel={e => { e.preventDefault(); if (!busy) onClose(); }}>
    <form onSubmit={save}>
      <div className="calendar-dialog-heading"><h2 id="event-heading">{form.id ? 'Edit event' : 'New event'}</h2>
        <button type="button" disabled={busy} aria-label="Close event dialog" onClick={onClose}>×</button></div>
      <p className="calendar-muted">Times are shown in {zone}.</p>
      {error && <p role="alert" className="calendar-error">{error}</p>}
      <fieldset disabled={busy} style={{ border: 0, padding: 0 }}><label>Title<input ref={titleInput} required maxLength={150} {...field('title')} /></label>
      <div className="calendar-form-row">
        <label>Start<input required type="datetime-local" {...field('start')} /></label>
        <label>End<input required type="datetime-local" {...field('end')} /></label>
      </div>
      <label>Description<textarea rows={3} maxLength={5000} {...field('description')} /></label>
      <div className="calendar-form-row">
        <label>Category<select {...field('eventType')}>{categories.map(c => <option key={c} value={c}>{label(c)}</option>)}</select></label>
        <label>Status<select {...field('status')}>{statuses.map(s => <option key={s} value={s}>{label(s)}</option>)}</select></label>
      </div>
      <label>Location<input maxLength={255} {...field('location')} /></label>
      </fieldset>
      <div className="calendar-dialog-actions">
        {form.id && <button type="button" className="calendar-danger" disabled={busy} onClick={remove}>Delete event</button>}
        <button type="button" disabled={busy} onClick={onClose}>Cancel</button>
        <button className="calendar-primary" disabled={busy || !form.title.trim()} type="submit">{busy ? 'Saving…' : 'Save event'}</button>
      </div>
    </form>
  </dialog>;
}

export default function Calendar() {
  const [anchor, setAnchor] = useState(() => new Date());
  const [view, setView] = useState('month');
  const [revision, setRevision] = useState(0);
  const [result, setResult] = useState({ key: '', events: [], error: '' });
  const [draft, setDraft] = useState(null);
  const dates = viewDates(anchor, view);
  const from = dates[0].toISOString(), to = addDays(dates[dates.length - 1], 1).toISOString();
  const loadKey = from + '/' + to + '/' + revision;
  const loading = result.key !== loadKey;
  const events = loading ? [] : result.events;
  const error = loading ? '' : result.error;

  useEffect(() => {
    let active = true;
    getCalendarEvents(from, to).then(
      data => { if (active) setResult({ key: loadKey, events: data, error: '' }); },
      err => { if (active) setResult({ key: loadKey, events: [], error: err.message }); }
    );
    return () => { active = false; };
  }, [from, to, loadKey]);

  function addEvent(date = anchor) {
    const start = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 9);
    const end = new Date(start); end.setHours(10);
    setDraft({ title: '', description: '', start: toLocalInput(start), end: toLocalInput(end), eventType: 'MEETING', status: 'SCHEDULED', location: '' });
  }
  function editEvent(event) {
    setDraft({ id: event.id, title: event.title, description: event.description || '',
      start: toLocalInput(event.startTime), end: toLocalInput(event.endTime),
      eventType: event.eventType || 'OTHER', status: event.status || 'SCHEDULED', location: event.location || '' });
  }
  const heading = view === 'day' ? anchor.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
    : view === 'week' ? dates[0].toLocaleDateString() + ' – ' + dates[6].toLocaleDateString()
      : anchor.toLocaleDateString(undefined, { year: 'numeric', month: 'long' });

  return <section className="company-calendar" aria-label="Company calendar">
    <header className="calendar-header"><div><h1>Calendar</h1><p className="calendar-muted">Workspace events · {zone}</p></div>
      <button className="calendar-primary" onClick={() => addEvent()}>New event</button></header>
    <div className="calendar-toolbar">
      <div><button aria-label="Previous period" onClick={() => setAnchor(navigateDate(anchor, view, -1))}>Previous</button>
        <button onClick={() => setAnchor(new Date())}>Today</button>
        <button aria-label="Next period" onClick={() => setAnchor(navigateDate(anchor, view, 1))}>Next</button></div>
      <h2 aria-live="polite">{heading}</h2>
      <div aria-label="Calendar view">{['month', 'week', 'day'].map(v => <button key={v} aria-pressed={view === v} onClick={() => setView(v)}>{label(v)}</button>)}</div>
    </div>
    {loading && <p role="status">Loading events…</p>}
    {error && <div role="alert" className="calendar-error">{error} <button onClick={() => setRevision(n => n + 1)}>Retry</button></div>}
    <div className="calendar-scroll"><div className={'calendar-grid calendar-' + view} aria-busy={loading}>
      {dates.map(date => {
        const dayEvents = eventsForDay(events, date);
        const today = dateKey(date) === dateKey(new Date());
        return <section key={dateKey(date)} aria-label={date.toLocaleDateString()} className={'calendar-day' + (date.getMonth() !== anchor.getMonth() && view === 'month' ? ' calendar-outside' : '')}>
          <button className={'calendar-date' + (today ? ' calendar-today' : '')} aria-label={'Add event on ' + date.toLocaleDateString()} onClick={() => addEvent(date)}>
            <span>{date.toLocaleDateString(undefined, { weekday: 'short' })}</span> {date.getDate()}</button>
          {dayEvents.map(event => <button key={event.id} className={'calendar-event calendar-' + event.eventType?.toLowerCase() + (event.status === 'CANCELLED' ? ' calendar-cancelled' : '')}
            onClick={() => editEvent(event)} title={event.title + ' · ' + label(event.status || 'SCHEDULED')}>
            <time>{new Date(event.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</time>
            <strong>{event.title}</strong><small>{label(event.eventType || 'OTHER')} · {label(event.status || 'SCHEDULED')}</small>
          </button>)}
          {!loading && !error && !dayEvents.length && view !== 'month' && <p className="calendar-muted">No events</p>}
        </section>;
      })}
    </div></div>
    {draft && <EventDialog draft={draft} onClose={() => setDraft(null)} onSaved={() => { setDraft(null); setRevision(n => n + 1); }} />}
  </section>;
}
