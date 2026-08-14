# box-showcase

Showcase of the native Jakarta Faces components from the `box` lib
(module `br.edu.iffar:box`, reusable across IFFar projects): one
demonstration page per component, with a live example, an XHTML code
snippet to reproduce it, and a description of its attributes.

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 25 |
| Maven | 3.9+ |

**No database** — no `box` component needs persistence, unlike
`bpm-app`.

## How to run

From the repository root:

```bash
cd box-showcase
mvn -o liberty:run
```

- Application: **http://localhost:9081**
- Ctrl+C to stop (`liberty:run` runs in the foreground).
- For development with Java/Facelets hot-reload, use `mvn -o liberty:dev`
  instead of `liberty:run`.
- Different port than `bpm-app` (9080/9443): both can run at the same
  time without conflict.

If this is the first time running the reactor (or after a `git pull`
that brought in a new module/dependency), build everything first, from
the repository root:

```bash
mvn clean install -DskipTests
```

(No `-o` flag the first time: the `download-maven-plugin` needs network
access to download Quill/Bootstrap Icons/FullCalendar. See `BUILD.md` in
the repository root for more details on this mechanism.)

## What's in the showcase

| Page | Component | Demonstrates |
|---|---|---|
| `/index.xhtml` | — | List with a link to each page below |
| `/panel.xhtml` | `b:panel` | Box with an optional title |
| `/confirm.xhtml` | `box-confirm` | Inline confirmation before an action, via a `<box-confirm>` custom element nested in an `h:commandLink` (no Faces component) |
| `/editor.xhtml` | `b:editor` | Rich text editor (Quill): bold, italic, font, color, lists, pasting images, etc. |
| `/growl.xhtml` | `b:growl` | Messages (`FacesMessage`) shown as floating toasts, equivalent to `p:growl` |
| `/schedule.xhtml` | `b:schedule` | Event calendar (FullCalendar): month/week/day, create/move/resize an event, click an event |
| `/schedule2.xhtml` | `b:schedule2` | Same idea as `b:schedule`, with no external lib (month view only, no resizing) — for comparison |
| `/popup.xhtml` | `b:popup` | Modal popup based on the native `<dialog>` element |
| `/datatable.xhtml` | `b:datatable` | Table with lazy pagination, sorting and per-column filtering |

Each component page has an "Attributes" and/or "Client behaviors" table
documenting the API, and an `EXAMPLE` block with the exact XHTML snippet
used in the demonstration itself.

## State of the demonstrations

The beans behind each page (`ConfirmDemoBean`, `EditorDemoBean`,
`ScheduleDemoBean`, `Schedule2DemoBean`) are `@SessionScoped`: the state
(deleted items, editor content, moved events) persists for as long as
the browser session lasts, and resets in a new incognito tab or after
the session expires. No need to manually clean anything up — there's no
database behind it, it's all in memory.

## End-to-end tests

`box-showcase/src/e2e-test/java/.../e2e/*IT.java` (Playwright) cover the
components' interactive behaviors (editor staying editable after an
ajax update, confirmation popups, dragging an event on the calendar,
etc.). They run via `mvn verify -Pe2e` — see the "Tests" section of
`BUILD.md` in the repository root for details (why it's a separate
profile, how to install Playwright's Chromium the first time, etc.).
