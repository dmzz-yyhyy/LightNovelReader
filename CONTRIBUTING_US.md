[简体中文](CONTRIBUTING.md) | [繁體中文](CONTRIBUTING_TW.md) | **English**

# Contributing to LightNovelReader

Thank you for contributing to LightNovelReader! You can help by reporting issues, improving documentation or translations, fixing bugs, or implementing new features.

Before submitting code, please read this guide. It explains the project's rules for branches, versioning, commit messages, and Pull Requests (PRs).

## Before You Start

- You may submit a PR directly for a small, well-defined fix.
- For major features, architectural changes, plugin API changes, or UI redesigns, open an Issue or Discussion first and confirm the approach with the maintainers.
- If you plan to fix an existing Issue, leave a comment on it to avoid duplicated work.
- Bug reports should include the app version, Android version, data source, reproduction steps, and relevant logs whenever possible.
- Remove private information such as accounts, tokens, and bookshelf contents before sharing logs or screenshots.

Project URL: <https://github.com/dmzz-yyhyy/LightNovelReader>

## Development Environment

The project is built with Kotlin, Jetpack Compose, and Gradle. The main requirements are:

- JDK 21
- The Gradle Wrapper included in the repository
- An Android SDK compatible with the configuration in `app/build.gradle.kts`
- IntelliJ IDEA or Android Studio

After forking and cloning the repository, we recommend adding the upstream repository:

```bash
git remote add upstream https://github.com/dmzz-yyhyy/LightNovelReader.git
git fetch upstream
```

## Branch Model

The long-lived branches have the following roles:

| Branch | Purpose | Regular PRs accepted |
|---|---|---|
| `dev/<version>` | Day-to-day development for the next version; currently `dev/1.3` | Yes |
| `release/<version>` | Stabilization and patch maintenance for released or upcoming versions | Confirmed fixes only |
| `legacy` | Preserves the history of the legacy version | No |

Choose the correct target branch for your PR:

- New features, regular bug fixes, and refactoring: target the current `dev/<version>` branch.
- Fixes that must be included in a patch for an older version: confirm with the maintainers first, then target the corresponding `release/<version>` branch.
- Do not submit PRs to `legacy`.

Development branches must be short-lived and created from the intended PR target branch:

```text
feat/<issue>-<description>
fix/<issue>-<description>
refactor/<description>
perf/<description>
docs/<description>
ci/<description>
chore/<description>
l10n/<locale>
hotfix/<version>-<description>
```

Examples:

```text
feat/497-reader-typography
fix/485-wenku8-encoding
refactor/plugin-loader
hotfix/1.2.3-export-crash
```

Use lowercase English words in branch names and separate words with `-`. Include the Issue number when one exists.

## Versioning

The app version is defined in `app/build.gradle.kts`:

```kotlin
versionCode = x * 1_000_000 + y * 10_000 + z * 1_000 + build
versionName = "x.y.z"
```

For example:

```text
versionName = 1.3.0
versionCode = 1_03_00_009
```

The last three digits are the development build number for the current release line.

### When to Increment `versionCode`

The following changes generally require a `versionCode` increment:

- Changes to app behavior under `app/src/main/kotlin/**`.
- Build configuration changes that affect the installable APK.
- Changes for which a maintainer explicitly requests a new distributable build.

Documentation-only changes, CI configuration, store metadata, and automated localization updates generally do not require a version change unless a maintainer says otherwise.

### How to Increment It

The `versionCode` must be greater than the current value on the PR's **target branch**.

For example, if `dev/1.3` currently uses:

```text
1_03_00_009
```

the next code PR merged into that branch should use:

```text
1_03_00_010
```

A PR targeting `release/1.2.2` must be compared with `release/1.2.2`, not with `dev/1.3`.

When multiple PRs are developed in parallel, they may initially use the same build number. Before merging, synchronize your branch with the target branch. If another PR has already taken that number, increment it again. Do not change `versionName` merely to pass a check.

Maintainers handle the following changes while preparing a new release:

- Changing `versionName`.
- Changing the major, minor, or patch digits of `versionCode`.
- Resetting the final three-digit development build number.
- Creating the official release tag.

If the final three-digit build number is approaching `999`, contact the maintainers instead of changing the encoding formula yourself.

## Commit Messages

Commit messages follow the Conventional Commits format:

```text
<type>(<scope>): <short description>

[optional body: explain the reason, implementation, and compatibility impact]

[optional footer: reference an Issue or describe a breaking change]
```

The main allowed types are:

| Type | Purpose |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Refactoring that does not change external behavior |
| `perf` | Performance improvement |
| `docs` | Documentation |
| `test` | Tests |
| `build` | Build system, Gradle, or dependencies |
| `ci` | Continuous integration configuration such as GitHub Actions |
| `chore` | Other maintenance work |
| `l10n` | Translation and localization |
| `revert` | Revert an existing change |

Recommended scopes include:

```text
reader, bookshelf, source, api, plugin, epub,
proxy, compiler, ui, storage, deps, release
```

Examples:

```text
feat(reader): add paragraph spacing setting

fix(source-wenku8): fix GB18030 punctuation decoding

refactor(plugin): separate plugin loading from validation

build(deps): remove unused Maven repository

l10n(en): update app description
```

Use `!` for an incompatible change and explain it in the footer:

```text
feat(api)!: change plugin route return type

BREAKING CHANGE: plugins must migrate to the new Route.Book interface.
```

The description may be written in Chinese or English, but the type and scope must use lowercase English. Avoid vague subjects such as `temp`, `update`, or `fix issue`.

Keep each commit focused on one purpose. Do not mix unrelated formatting, dependency updates, refactoring, and feature changes in the same commit.

## Build and Test

Before submitting a PR, make sure the project can at least complete a Debug build.

Windows:

```powershell
.\gradlew.bat assembleDebug
```

macOS or Linux:

```bash
./gradlew assembleDebug
```

Add appropriate tests for new logic. For UI changes, include before-and-after screenshots or a screen recording in the PR, and verify the change on at least one representative Android version or device within the project's supported range.

## Pull Requests

A PR should contain one logical change. Create a Draft PR if the work is not yet complete.

Use the commit-message format for the PR title because the PR title becomes the Squash Commit title when merged:

```text
fix(epub): exclude unrelated chapters when exporting selected volumes
```

The PR description should include at least:

```markdown
## What Changed

## Why

## Verification

## Screenshots or Screen Recording

close #<issue-number>
```

Before submitting, confirm that:

- The PR targets the correct branch.
- Your branch is synchronized with the latest target-branch changes.
- A code PR has a `versionCode` greater than the target branch's value.
- The build, tests, and CI checks pass.
- The PR contains no unrelated files or broad formatting changes.
- New UI text has been prepared for localization.
- Any API or plugin compatibility changes are clearly described.
- The PR is linked to the corresponding Issue.

Maintainers may request implementation changes, a smaller PR, or additional tests. A maintainer will merge the PR after approval; you may then delete the development branch.

## Translation

The project uses Crowdin to manage translations. Routine translation changes should be made through the project's Crowdin workflow to avoid conflicts with automatically managed translation files.

When adding UI text during development, create only the default English entry in `app/src/main/res/values/strings.xml`, using a meaningful and reusable resource name. Submit translations for other languages through Crowdin.
