# Security policy

## Security contact

Report a vulnerability privately. That form is the security contact:

<https://github.com/pirlruc/mat-aventuras/security/advisories/new>

Do not open a public issue for a vulnerability.

Include the affected version (for example `0.2.0`) and a way to reproduce it. You will get an acknowledgement within 7 days.

## Supported versions

Only the latest GitHub Release is supported, until the next release replaces it. There is no calendar end date.

Security fixes land on the default branch and ship in the next release. Older tags are not patched in place.

## Published vulnerabilities

No vulnerability advisory has been published. A confirmed vulnerability is published as a [GitHub Security Advisory](https://github.com/pirlruc/mat-aventuras/security/advisories) on this repository after a fix ships.

A component vulnerability that is present in a dependency and does not affect this app is recorded in [docs/VEX.md](docs/VEX.md). None are recorded.

## Secrets

The release signing key and its password stay outside this repository, in a backup the maintainer controls. Continuous integration reads them from GitHub Actions secrets. Pull requests from forks do not receive those secrets. The release key is not rotated, because a new key cannot update an app already installed with the old one.

## Dependencies and static analysis

A known vulnerable dependency, a malicious dependency, or a disallowed license blocks the pull request. A release is not cut while `dependency-review` or `supply-chain` is failing. An exception is a recorded suppression with a reason.

A semgrep finding from the required `secrets-sast` job blocks the merge. A suppression names the finding and why it is not exploitable. That job is required on every change. CodeQL and MobSF are additional scans. They are not the required gate.

## What this covers

This repository's code and the published release APKs, including their checksums and the dependency bill.
