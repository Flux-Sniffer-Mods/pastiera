# Flux Keyboard changelog

Changes since **Pastiera 0.85**, the last official Pastiera release (May 2026): first what Flux Keyboard adds, then [what the Pastiera team has built since](#from-the-pastiera-team-since-085), which Flux Keyboard includes.

Flux Keyboard is an unofficial fork of [Pastiera](https://github.com/palsoftware/pastiera), the physical-keyboard input method created by Andrea Palumbo (PalSoftware) and developed by Andrea Palumbo, Patrick Zauner and the Pastiera contributors. All credit for Pastiera itself goes to them; this page lists only what the fork changes. Flux Keyboard is not affiliated with or endorsed by the Pastiera team, and like Pastiera it is licensed under the GNU GPL v3.

The fork is tuned for the Unihertz Titan 2 Elite and works on any phone with a hardware keyboard. It installs alongside Pastiera (app ID `io.github.fluxsniffermods.fluxkeyboard`) and starts from a configuration made on a Titan 2 Elite. Sections and items are ordered with the biggest differences first.

Flux Keyboard was called **Pastiera Flux** (app ID `it.palsoftware.pastiera.flux`) until September 2026; it was renamed at the Pastiera team's request. The new app ID makes it a separate app: install it, restore a backup from Pastiera Flux, then uninstall Pastiera Flux.

## Emoji, symbols and GIFs

- **Emoji picker key**: a dedicated key (Right Shift by default), assigned by pressing it. A modifier key opens the picker when released, so it still works in chords like Ctrl+Shift+Q.
- **GIF search** (KLIPY) in the picker and on the emoji layer, with favourites, recents and caching.
- **Symbol search** across every Unicode symbol.
- **Emoji layer**: search, a Recents key, and **profiles** for common situations (chatting, work, social…) that can follow the app you're in.
- Emoji beyond the system font, and names for every emoji in search.
- A search key (⌕) on every panel; letters type the layer's mappings unless type-to-search is on.

## Typing

- **Spell checker**: Flux Keyboard as Android's spell checker, underlining typos in any app.
- **Inline autofill**: password manager chips in the suggestion bar.
- **Pick suggestions from the keyboard**: Ctrl+Shift+Q, W or E takes the left, middle or right suggestion (Ctrl+1/2/3 on keyboards with a number row). Either Shift works, including Right Shift while it's the emoji key.
- **Backspace undoes an auto-replace** and keeps the space after it; text replacements can be undone with auto-replace off too.
- **Paste suggestion**: what you just copied, offered in the next text field.
- **Clean pasted links**: pasted links lose tracking (utm_, fbclid, si…) and open the full site rather than the mobile one.
- **Emoji suggestions**: an emoji for the word you're typing.
- **Exact typing** (Apps): in the apps you pick (SSH clients, code editors, AI agents) nothing rewrites what you type: no auto-correct, text replacements, auto-capitals, double-space full stop or automatic spaces. Optionally also wherever an app asks for no suggestions.
- **Snippets** fill in `{date}`, `{time}`, `{datetime}`, `{isodate}`, `{day}` and `{clipboard}`.
- **Smart toggle**: Alt and Ctrl switch themselves off by context.
- **Voice input keeps listening** through pauses until you stop it or stay silent.
- **Incognito typing**: learn nothing when an app asks, or always.
- **Ctrl+Shift+Space** switches language backwards.
- No automatic Shift in scripts without capitals (Thai, Arabic, CJK…).
- **Bold suggestions** option.

## Apps

- **App shortcuts**: the same shortcuts in every app, suggested per category, plus each app's own launcher shortcuts and settings (Ctrl+Alt+1–4, Ctrl+,).
- **Terminal mode**: Termux gets Pastiera's Alt and SYM, a real Ctrl, a hidden keyboard and a choice of what the emoji key sends.
- **Enter per app** gets standards by app category, next to App shortcuts.
- **Hidden-keyboard apps** (Termux:X11, Niagara Launcher by default), with LEDs and panels per app.
- The **QuickLauncher** opens from other apps (key mappers, Tasker) and the app icon.
- **Linux desktop** keyboard layout from Pastiera's Alt map and SYM page.

## Titan 2 Elite, status bar and LEDs

- **Recommended settings** apply a configuration made on a Titan 2 Elite, after saying how much would change.
- A status bar fitted to the rounded display: filled corners, straight outer buttons, a 5 dp lift above four LEDs, and room around the SYM screens.
- Every Titan 2 Elite setting on one screen.
- **Per-LED colours**, a clear active-to-locked jump and an optional sweeping gradient when locked.
- A **customisable menu bar** (which buttons, in what order) with a GIF button.
- Indicators are text symbols, never emoji.

## Settings and privacy

- **Updates from this fork**: Flux Keyboard checks this repository's latest release, not upstream's, and offers the APK straight from the notice.
- Settings grouped by task, ordered by usefulness, searchable, with rows sized to their text.
- **Offline mode**: nothing in the keyboard goes online.
- Settings for hardware the phone doesn't have (Clicks keyboard, Titan 2 layout) stay hidden.
- Developer options gather calibration and debug tools.

## SYM layers and variations

- **Device SYM layer editor** with curated and custom profiles.
- The pencil on the symbol panels edits that layer; holding it edits the variations mapping.
- Dev's choice static variations by default.

## From the Pastiera team since 0.85

Flux Keyboard is built on Pastiera's main branch (September 2026), so it also includes what the Pastiera team has added since their 0.85 release. That work is theirs:

- **Clicks Power Keyboard** support: controls, firmware status, and SYM profiles for Razr and Pixel.
- An **on-screen keyboard mode** based on AOSP, with themes, a number row, layout styles and long-press layers.
- **Keyboard themes** with an editor, draft themes, per-app theme assignment and transparent presets.
- **Titan 2 Elite geometry**: rounded corners on the keyboard and status bar, calibrated contours and a gapless mode.
- **Settings overhaul**, with search and shareable deep links.
- **Learned next-word suggestions**, bigrams, suggestions from several dictionaries, and adding words from substitutions and swipes.
- **Snippet expansion**, and emoji and symbol shortcodes.
- **Punctuation spacing** (French spacing, closing brackets, commas), smart punctuation and mid-word quote replacement.
- **Native trackpad gestures** on the Titan 2 and Titan 2 Elite, with separate sensitivities.
- **Original Titan** keyboard profile and Alt keymap.
- A **unified QuickLauncher** with Niagara search, and Home and document navigation actions.
- Configurable **modifier latching and indicators**, Alt+Enter layout switching, Shift for Nav Mode, **bounce keys** and tap haptics.
- **Greek translation**, and configurable status buttons for the compact mode (Pastierina, called Solderina here).
- Clipboard history **hidden while the phone is locked**.
- **Safer backups** (themes and typing sounds included) and many fixes: suggestions in Telegram, emoji search, Firefox accents, Ctrl shortcuts in number fields and more.

## Upstream issues addressed

palsoftware/pastiera #108, #217, #267, #278, #282, #292, #302, #310, #316, #317.

## Builds

Signed APKs come from the fork build workflow on GitHub Actions. Each successful build is published as a GitHub release (tag `flux/v<version>`) with these notes, replaces the previous release, and also ships the notes as `RELEASE_NOTES.md` in the build artifact. Flux Keyboard builds are signed with the fork's own key and don't update, or get updated by, official Pastiera.
