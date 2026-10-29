# Private GitHub backup — prepared, not pushed

Intended repository: `https://github.com/bodiujjamanrafi/AntiReel-AI` (privacy/existence not verified).

The local `gh auth status` reports no authenticated GitHub host. A browser sign-in does not authenticate this CLI, and no connected GitHub/browser API is exposed to this session. The original workspace has no existing Git history; its root `.git` path is read-only under the workspace policy. A sanitized, buildable Git checkout is therefore prepared separately at `/tmp/antireel-github-122` and exported as `dist/AntiReel-AI-1.0.22-source.bundle`. No existing remote history was replaced.

The snapshot includes Android source/resources, Gradle wrapper/config, legal site source, public release-certificate fingerprint, documentation and tools. It excludes signing keys/passwords, local.properties, .env files, IDE/Gradle caches, APK/AAB outputs, generated images and local diagnostics. The public release certificate fingerprint is not a private key. Release credentials must be kept and backed up privately outside Git.

## Finish with your authenticated account

```sh
# Run on your own machine. Authenticate interactively; do not paste a token in chat.
gh auth login --hostname github.com --web
# Clone the delivered Git bundle into a new directory, preserving the prepared commit.
git clone -b main /absolute/path/AntiReel-AI-1.0.22-source.bundle AntiReel-AI
cd AntiReel-AI
# The clone origin is the local bundle, not an existing GitHub repository.
git remote remove origin
bash tools/publish_private_github.sh --publish
```

The helper verifies the account is `bodiujjamanrafi`, checks reachable history for common secrets, requires a clean checkout, creates the repository as PRIVATE if missing, and checks the remote commit/privacy after pushing. It stops if an existing repository is public, has an unexpected origin, or already has history. In that case fetch/review/merge existing history normally; do not force-push, delete or replace it. A failed create or push is not success.

No GitHub repository was created and no remote push was attempted in this session. Access is the remaining blocker. After success, record the actual repository URL, visibility and matching commit hash in the release report.

## Local validation

Run `python3 tools/audit_source_secrets.py` after staging and `python3 tools/audit_source_secrets.py --history` after committing. This pattern scan is one safeguard, not a guarantee that all possible secrets have been detected. Review the file list too. Never add an entire project blindly if local signing/data files have appeared since this snapshot.
