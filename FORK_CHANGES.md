# Pastiera Flux: what this fork adds

Pastiera Flux is a fork of [Pastiera](https://github.com/palsoftware/pastiera) tuned for the
Unihertz Titan 2 Elite, and useful on any phone with a hardware keyboard. It installs alongside
Pastiera (app ID `it.palsoftware.pastiera.flux`) and starts from a configuration made on a Titan
2 Elite.

## Typing

- **Exact typing** (Apps): in the apps you pick (SSH clients, code editors, AI agents) nothing
  rewrites what you type: no auto-correct, text replacements, auto-capitals, double-space full
  stop or automatic spaces. Optionally also wherever an app asks for no suggestions.
- **Pick suggestions from the keyboard**: Ctrl+Shift+Q, W or E takes the left, middle or right
  suggestion (Ctrl+1/2/3 on keyboards with a number row).
- **Backspace undoes an auto-replace** and keeps the space after it; text replacements can be
  undone with auto-replace off too.
- **Emoji suggestions**: an emoji for the word you're typing.
- **Spell checker**: Pastiera as Android's spell checker, underlining typos in any app.
- **Inline autofill**: password manager chips in the suggestion bar.
- **Paste suggestion**: what you just copied, offered in the next text field.
- **Clean pasted links**: pasted links lose tracking (utm_, fbclid, si…) and open the full site
  rather than the mobile one.
- **Snippets** fill in `{date}`, `{time}`, `{datetime}`, `{isodate}`, `{day}` and `{clipboard}`.
- **Smart toggle**: Alt and Ctrl switch themselves off by context.
- **Incognito typing**: learn nothing when an app asks, or always.
- **Voice input keeps listening** through pauses until you stop it or stay silent.
- **Bold suggestions** option.
- No automatic Shift in scripts without capitals (Thai, Arabic, CJK…).
- **Ctrl+Shift+Space** switches language backwards.

## Emoji, symbols and GIFs

- **Emoji picker key**: a dedicated key (Right Shift by default), assigned by pressing it.
- Emoji beyond the system font, and names for every emoji in search.
- **Emoji layer**: search, a Recents key, and **profiles** for common situations (chatting,
  work, social…) that can follow the app you're in.
- **GIF search** (KLIPY) in the picker and on the emoji layer, with favourites, recents and
  caching.
- **Symbol search** across every Unicode symbol.
- A search key (⌕) on every panel; letters type the layer's mappings unless type-to-search is on.
- **Offline mode**: nothing in Pastiera goes online.

## Apps

- **App shortcuts**: the same shortcuts in every app, suggested per category, plus each app's
  own launcher shortcuts and settings (Ctrl+Alt+1–4, Ctrl+,).
- **Enter per app**, with standards by app category.
- **Terminal mode**: Termux gets Pastiera's Alt and SYM, a real Ctrl, a hidden keyboard and a
  choice of what the emoji key sends.
- **Hidden-keyboard apps** (Termux:X11, Niagara Launcher by default), with LEDs and panels per app.
- **Linux desktop** keyboard layout from Pastiera's Alt map and SYM page.
- The **QuickLauncher** opens from other apps (key mappers, Tasker) and the app icon.

## Titan 2 Elite, status bar and LEDs

- A status bar fitted to the rounded display: filled corners, straight outer buttons, a 5 dp lift
  above four LEDs, and room around the SYM screens.
- **Per-LED colours**, a clear active-to-locked jump and an optional sweeping gradient when locked.
- A **customisable menu bar** (which buttons, in what order) with a GIF button.
- Indicators are text symbols, never emoji.

## SYM layers and variations

- **Device SYM layer editor** with curated and custom profiles.
- Dev's choice static variations by default.
- The pencil on the symbol panels edits the variations mapping.

## Settings

- Settings grouped by task, ordered by usefulness, searchable, with rows sized to their text.
- Every Titan 2 Elite setting on one screen.
- **Recommended settings** apply the default configuration, after saying how much would change.
- Settings for hardware the phone doesn't have (Clicks keyboard, Titan 2 layout) stay hidden.
- Developer options gather calibration and debug tools.

## Upstream issues addressed

palsoftware/pastiera #108, #217, #267, #278, #282, #292, #302, #310, #316, #317.

## Builds

Signed APKs come from the fork build workflow on GitHub Actions; each successful build replaces
the previous one, and ships these notes as `RELEASE_NOTES.md`.
