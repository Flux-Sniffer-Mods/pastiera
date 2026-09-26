#!/usr/bin/env python3
"""Versions and release notes for Flux Keyboard builds.

  flux_release.py version <branch> [version_name]
      Prints kind=, name= and code= lines for $GITHUB_OUTPUT. The flux-release branch builds
      full releases: the newest version in whats_new.json's "releases". Every other branch
      builds dev builds: the next version after that, stamped with the build time
      (0.93-flux.yyyyMMddHHmm). A given version_name must be of the branch's kind.

  flux_release.py notes <version> <previous tag or ""> <commit>
      Prints release notes listing only what changed since the previous build: for a full
      release the previous full release, for a dev build the previous build of either kind.

Entries in whats_new.json are either text or {"text", "after": "yyyyMMddHHmm"}: new since the
build made at "after". Full releases carry no time in their name, so "releases" maps each one
to the time it was built.
"""
import datetime
import json
import re
import sys

WHATS_NEW = "app/src/main/assets/fork/whats_new.json"
RELEASE_BRANCH = "flux-release"
SECTIONS = [("highlights", "New"), ("improvements", "Improved"), ("bugFixes", "Fixed"),
            ("upstream", "From Pastiera")]


def is_release_branch(branch):
    return branch.lower() == RELEASE_BRANCH.lower()


def load():
    with open(WHATS_NEW, encoding="utf-8") as f:
        return json.load(f)


def parse(version):
    """0.92 -> (0, 92); 0.93-flux.202610011200 -> (0, 93)."""
    m = re.fullmatch(r"(\d+)\.(\d+)(?:-flux\.(\d{12}))?", version)
    if not m:
        sys.exit(f"Not a Flux Keyboard version: {version}")
    return int(m.group(1)), int(m.group(2))


def is_dev(version):
    return "-flux." in version


def code(version):
    major, minor = parse(version)
    return major * 100 + minor


def newest_release(data, below=None):
    versions = [v for v in data.get("releases", {}) if below is None or parse(v) < parse(below)]
    return max(versions, key=parse, default=None)


def version_cmd(branch, given):
    data = load()
    release = newest_release(data)
    if given:
        name = given
        parse(name)
        if is_release_branch(branch) and is_dev(name):
            sys.exit(f"{RELEASE_BRANCH} builds full releases, not the dev build {name}")
        if not is_release_branch(branch) and not is_dev(name):
            sys.exit(f"{branch} builds dev builds (x.yy-flux.<time>); full releases come from {RELEASE_BRANCH}")
        if is_dev(name) and release and parse(name) <= parse(release):
            # 0.92-flux.<time> counts as older than 0.92, so it would never be offered as an update
            sys.exit(f"Dev builds after {release} need a newer version than it, like {parse(release)[0]}.{parse(release)[1] + 1:02d}-flux.<time>")
    elif is_release_branch(branch):
        if release is None:
            sys.exit(f"No releases in {WHATS_NEW}")
        name = release
    else:
        major, minor = parse(release) if release else (0, 90)
        stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%d%H%M")
        name = f"{major}.{minor + 1:02d}-flux.{stamp}"
    if not is_dev(name) and name not in data.get("releases", {}):
        sys.exit(f'Add "{name}": "<yyyyMMddHHmm>" to "releases" in {WHATS_NEW} before releasing it')
    print(f"kind={'dev' if is_dev(name) else 'release'}")
    print(f"name={name}")
    print(f"code={code(name)}")


def stamp_of(data, version):
    if is_dev(version):
        return int(version.rsplit(".", 1)[1])
    stamp = data.get("releases", {}).get(version)
    return int(stamp) if stamp else None


def notes_cmd(version, previous_tag, commit):
    data = load()
    previous = previous_tag.removeprefix("flux/v") if previous_tag else None
    if not is_dev(version):
        # A full release lists everything since the full release before it
        previous = newest_release(data, below=version)
    since = stamp_of(data, previous) if previous else None

    out = []
    kind = "Dev build" if is_dev(version) else "Release"
    out.append(f"{kind} `{version}` · built from {commit[:7]} · the APK is under **Assets** below.")
    out.append("")
    if since is None:
        out.append("## What's new")
    else:
        what = "dev build" if is_dev(previous) else "release"
        out.append(f"## Changes since {previous} ({what})")
    any_entry = False
    for key, title in SECTIONS:
        entries = []
        for entry in data.get(key, []):
            if isinstance(entry, dict):
                text, after = entry.get("text", ""), int(entry.get("after", "0") or 0)
            else:
                text, after = entry, 0
            if text and (since is None or after >= since):
                entries.append(text)
        if entries:
            any_entry = True
            out.append("")
            out.append(f"### {title}")
            out.extend(f"- {text}" for text in entries)
    if not any_entry:
        out.append("")
        out.append("Behind-the-scenes changes only.")
    out.append("")
    out.append("<details><summary>Everything Flux Keyboard adds over Pastiera</summary>")
    out.append("")
    with open("FORK_CHANGES.md", encoding="utf-8") as f:
        out.extend(line.rstrip("\n") for line in f.readlines()[1:])
    out.append("")
    out.append("</details>")
    print("\n".join(out))


if __name__ == "__main__":
    args = sys.argv[1:]
    if args[:1] == ["version"] and len(args) in (2, 3):
        version_cmd(args[1], args[2] if len(args) == 3 else "")
    elif args[:1] == ["notes"] and len(args) == 4:
        notes_cmd(args[1], args[2], args[3])
    else:
        sys.exit(__doc__)
