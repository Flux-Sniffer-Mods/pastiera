# Flux Keyboard changelog

Changes since **Pastiera 0.85**, the last official Pastiera release (May 2026): first what Flux Keyboard adds, then [what the Pastiera team has built since](#from-the-pastiera-team-since-085), which Flux Keyboard includes.

Flux Keyboard is an unofficial fork of [Pastiera](https://github.com/palsoftware/pastiera), the physical-keyboard input method created by Andrea Palumbo (PalSoftware) and developed by Andrea Palumbo, Patrick Zauner and the Pastiera contributors. All credit for Pastiera itself goes to them; this page lists only what the fork changes. Flux Keyboard is not affiliated with or endorsed by the Pastiera team, and like Pastiera it is licensed under the GNU GPL v3.

The fork is tuned for the Unihertz Titan 2 Elite and works on any phone with a hardware keyboard. It installs alongside Pastiera (app ID `io.github.fluxsniffermods.fluxkeyboard`) and starts from a configuration made on a Titan 2 Elite. Sections and items are ordered with the biggest differences first.

Flux Keyboard was called **Pastiera Flux** (app ID `it.palsoftware.pastiera.flux`) until September 2026; it was renamed at the Pastiera team's request. The new app ID makes it a separate app: install it, restore a backup from Pastiera Flux, then uninstall Pastiera Flux.

## Emoji, symbols and GIFs

- **Emoji picker key**: a dedicated key (Right Shift by default), assigned by pressing it. A modifier key opens the picker when released, so it still works in chords like Ctrl+Shift+Q.
- **Hold or tap the emoji key**: hold it and press a key to type that key's emoji from the emoji layer; optionally, one tap makes the next key do the same, and a second tap opens the emoji screen. Held without choosing anything, it just lets go.
- **Tap SYM for one symbol** (option): one tap makes the next key type its symbol without opening the symbols; a second tap opens them. Holding SYM works as before, and held without choosing anything it just lets go.
- **GIF search** (KLIPY) in the picker and on the emoji layer, with favourites, recents and caching.
- **Symbol search** across every Unicode symbol.
- **Emoji layer**: search, a Recents key, and **profiles** for common situations (chatting, work, social…) that can follow the app you're in.
- Search, Recents and GIF keys on the layers have a tint of their own, and nothing in the default layers sits under them.
- Emoji beyond the system font, and names for every emoji in search.
- A search key (⌕) on every panel; letters type the layer's mappings unless type-to-search is on.

## Typing

- **Spell checker**: Flux Keyboard as Android's spell checker, underlining typos in any app.
- **Inline autofill**: password manager chips in the suggestion bar.
- **One-time codes**: a code from a notification (sign-in, bank, delivery) is offered as a chip in the next text field for three minutes. Off until you give it notification access.
- **Pick suggestions from the keyboard**: Ctrl+Shift+Q, W or E takes the left, middle or right suggestion (Ctrl+1/2/3 on keyboards with a number row). Either Shift works, including Right Shift while it's the emoji key.
- **Trackpad swipes**: optionally, swipe left, up or right anywhere on the trackpad for the left, middle or right suggestion, and swipe down to delete the previous word. Short and slanted swipes count, whichever way they mostly go.
- **Backspace undoes an auto-replace** and keeps the space after it; text replacements can be undone with auto-replace off too.
- **Automatic Shift by field type**: choose which kinds of text field start with a capital (text, names and addresses by default; search, links and email addresses off). A kind that's off gets no automatic Shift, even when an app asks for capitals.
- **Paste suggestion**: what you just copied, offered in the next text field.
- **Clean pasted links**: pasted links lose tracking (utm_, fbclid, si…) and open the full site rather than the mobile one.
- **Emoji suggestions**: an emoji for the word you're typing.
- **Exact typing** (Apps): in the apps you pick (SSH clients, code editors, AI agents) nothing rewrites what you type: no auto-correct, text replacements, auto-capitals, double-space full stop or automatic spaces. Optionally also wherever an app itself asks for no suggestions.
- **Remember the language per app**: each app gets back the language you last typed in there.
- **Snippets** fill in `{date}`, `{time}`, `{datetime}`, `{isodate}`, `{day}` and `{clipboard}`.
- **Smart toggle**: Alt and Ctrl switch themselves off by context.
- **Voice input keeps listening** through pauses until you stop it or stay silent.
- **Incognito typing**: learn nothing when an app asks, or always.
- **Ctrl+Shift+Space** switches language backwards.
- No automatic Shift in scripts without capitals (Thai, Arabic, CJK…).
- **Bold suggestions** option.

## Keyboard layouts

- **Edit layouts in the app**: tap a key to change what it types with and without Shift, then save, restore the original, save as a new layout or **export** it as JSON. No web editor needed.

## Apps and the quick launcher

- **App shortcuts**: the same shortcuts in every app, suggested per category, plus each app's own launcher shortcuts and settings (Ctrl+Alt+1–4, Ctrl+,).
- **Quick launcher**: the built-in one by default, in the keyboard's theme colours, with apps' own long-press shortcuts ("New message", "Scan QR code") as results.
- **Niagara search as the quick launcher** (option): Back, the key or the gesture, before opening anything returns to the app you opened it from, and on Niagara's home screen the built-in quick launcher opens instead.
- **Search bars wait for typing** (option): when an app opens with its search bar focused, the keyboard bar stays hidden until you type or tap the bar.
- **Terminal mode**: Termux gets the keyboard's Alt and SYM, a real Ctrl, a hidden keyboard and a choice of what the emoji key sends, including Alt.
- **Enter per app** gets standards by app category (chat apps use your messaging preset, email apps send with Ctrl+Enter), next to App shortcuts.
- **Hidden-keyboard apps** (Niagara Launcher by default; Termux:X11 and others from the tutorial), with LEDs and panels per app. SYM chords reach the keyboard there, so SYM + Space opens the quick launcher instead of the symbols panel.
- The **QuickLauncher** opens from other apps (key mappers, Tasker) and the app icon.
- **Linux desktop** keyboard layout from the keyboard's Alt map and SYM page.

## Titan 2 Elite, status bar and LEDs

- **Recommended settings** apply a configuration made on a Titan 2 Elite, after saying how much would change. Settings that need a permission or another app (one-time codes, Niagara search, hidden-keyboard apps) start off and are set up from the tutorial.
- A status bar fitted to the rounded display: filled corners, straight outer buttons, a 5 dp lift above the LEDs, and room around the SYM screens.
- Every Titan 2 Elite setting on one screen, and a **phone trackpad settings** shortcut (also a quick launcher command) to the phone's Keyboard gestures page, or Settings search with it ready to paste.
- **Per-LED colours**, a clear active-to-locked jump and an optional sweeping gradient when locked, set from one table with Off, Active and Locked columns.
- **SYM's LED** is lit while SYM is held and locked while it's tapped for one symbol or the symbols are open. A **fifth LED** does the same for the emoji key (on by default, off in Settings); all five LEDs share the width equally.
- **Wallpaper colours** (option): the keyboard takes its colours from your wallpaper.
- A **customisable menu bar** (which buttons, in what order) with a GIF button.

## Settings, tutorial and privacy

- **Flux Keyboard**: its own name, app ID, icon (a keyboard, in Niagara's icon packs too) and home screen fitted to the Titan 2 Elite, crediting and linking the original Pastiera. The compact mode is called **Solderina**.
- **A tutorial of its own**: one-step setup, every new feature, a page for making it yours, and the extras that need a permission, on pages that scroll. After an update, **What's new** shows alone, lists only what's new since the version you had, and closes with ✕ or Done.
- **Updates from this fork**: Flux Keyboard checks this repository's latest release, not upstream's, compares versions properly, and downloads and installs the update itself (Android asks before installing).
- **Restricted settings**: features that need the accessibility service or notification access say when Android blocks them for apps installed from a file, and open App info to lift the block.
- Settings grouped by task, ordered by usefulness, searchable, with rows sized to their text. Search keeps your query and your place when you open a result and come back, and nothing is focused on its own.
- Input Languages laid out like every other page.
- **Offline mode**: nothing in the keyboard goes online.
- Settings for hardware the phone doesn't have (Clicks keyboard, Titan 2 layout) stay hidden.
- Developer options gather calibration and debug tools.

## SYM layers and variations

- **Device SYM layer editor** with curated and custom profiles.
- The pencil on the symbol panels edits that layer's mapping; holding it on pages that have them edits the variations.
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
