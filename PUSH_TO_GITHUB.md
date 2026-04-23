# Push to GitHub — one-time setup

The sandbox where these files were generated cannot authenticate against GitHub,
so the actual `git push` has to happen from your machine. Everything is
staged and ready — the steps below take ~30 seconds.

## Quick path (fresh init, recommended)

Open a terminal in the `OfferDyme-LC` folder on your machine and run:

```bash
# 1. Clean up any half-initialized git artefacts from the sandbox
rm -rf .git offerdyne.bundle

# 2. Initialize + commit
git init -b main
git config user.email "sankethn@creditnirvana.ai"
git config user.name  "Sanketh Noolee"
git add .
git commit -m "Initial commit: OfferDyne Dynamic Settlement Optimizer"

# 3. Point to your repo and push
git remote add origin https://github.com/sankethnoolee/OfferDyme-LC.git
git branch -M main
git push -u origin main
```

If GitHub prompts for credentials, paste a Personal Access Token
(https://github.com/settings/tokens) as the password — GitHub removed
raw-password support in 2021.

## Alternative — restore from the bundle

A git bundle file (`offerdyne.bundle`) is included in the folder — it already
contains the initial commit with author metadata set. To use it:

```bash
rm -rf .git
git clone offerdyne.bundle .restore
mv .restore/.git .
rm -rf .restore offerdyne.bundle
git remote add origin https://github.com/sankethnoolee/OfferDyme-LC.git
git push -u origin main
```

Either path ends with the same result: your GitHub repo populated with the
full backend + frontend.
