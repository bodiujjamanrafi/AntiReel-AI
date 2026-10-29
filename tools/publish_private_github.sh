#!/usr/bin/env bash
# Run from the reviewed clean Git checkout after gh auth login. No force push.
set -euo pipefail
if [[ "${1:-}" != "--publish" ]]; then
  echo 'Review docs/GITHUB_SETUP.md, authenticate gh, then run: bash tools/publish_private_github.sh --publish'
  exit 2
fi
repo='bodiujjamanrafi/AntiReel-AI'
gh auth status >/dev/null
[[ "$(gh api user --jq .login)" == 'bodiujjamanrafi' ]] || { echo 'Wrong authenticated account; stopped.'; exit 1; }
git rev-parse --verify HEAD >/dev/null
[[ -z "$(git status --porcelain)" ]] || { echo 'Commit/review local changes before publishing.'; exit 1; }
python3 tools/audit_source_secrets.py --history
if gh repo view "$repo" --json isPrivate >/dev/null 2>&1; then
  [[ "$(gh repo view "$repo" --json isPrivate --jq .isPrivate)" == true ]] || { echo 'Existing repository is not private; stopped.'; exit 1; }
  if [[ "$(gh repo view "$repo" --json defaultBranchRef --jq '.defaultBranchRef.name // ""')" != '' ]]; then
    echo 'Existing repository has history. Fetch and review it before pushing; this helper will not replace it.'
    exit 1
  fi
  expected="https://github.com/$repo.git"
  if git remote get-url origin >/dev/null 2>&1; then
    [[ "$(git remote get-url origin)" == "$expected" ]] || { echo 'Existing origin differs; stopped.'; exit 1; }
  else
    git remote add origin "$expected"
  fi
  git push -u origin main
else
  # gh fails closed if the repository exists but is inaccessible or network fails.
  gh repo create "$repo" --private --description 'Native AntiReel AI Android app and legal/support site source' --source . --remote origin --push
fi
[[ "$(gh repo view "$repo" --json isPrivate --jq .isPrivate)" == true ]] || { echo 'Privacy verification failed.'; exit 1; }
local_sha="$(git rev-parse HEAD)"
remote_sha="$(git ls-remote origin refs/heads/main | cut -f1)"
[[ "$local_sha" == "$remote_sha" ]] || { echo 'Remote commit verification failed.'; exit 1; }
printf 'Verified private repository and matching commit: https://github.com/%s\n' "$repo"
