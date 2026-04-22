#!/usr/bin/env bash
set -euo pipefail

# ─────────────────────────────────────────────────────────────
# SparrowDesk SDK — Publish Script
#
# Publishes:
#   1. iOS XCFramework → GitHub Releases (for SPM)
#   2. Updates Package.swift with new checksum + version
#
# Android distribution is served by JitPack directly from git tags
# (see jitpack.yml) — no Android publish step is needed here.
#
# Prerequisites:
#   - GITHUB_TOKEN env var (with `contents: write` scope)
#   - gh CLI installed (for GitHub Releases)
#   - Xcode installed (for XCFramework + swift package compute-checksum)
#
# Usage:
#   ./scripts/publish.sh 0.2.0
# ─────────────────────────────────────────────────────────────

VERSION="${1:?Usage: ./scripts/publish.sh <version>}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

GITHUB_OWNER="${GITHUB_OWNER:-SparrowDesk}"
GITHUB_REPO="${GITHUB_REPO:-sparrowdesk-mobile-sdk}"

echo "============================================"
echo "  Publishing SparrowDesk SDK v${VERSION}"
echo "============================================"

# ── 1. Validate prerequisites ─────────────────────────────

if [ -z "${GITHUB_TOKEN:-}" ]; then
    echo "ERROR: GITHUB_TOKEN is not set."
    echo "Create a token at https://github.com/settings/tokens with scope: contents (write)"
    exit 1
fi

command -v gh >/dev/null 2>&1 || { echo "ERROR: gh CLI not found. Install: brew install gh"; exit 1; }

# ── 2. iOS: Build XCFramework + zip ───────────────────────

echo ""
echo "▸ [iOS] Building XCFramework..."
./gradlew :sparrowdesk-sdk:assembleSparrowDeskSDKXCFramework

echo "▸ [iOS] Creating zip..."
./gradlew :sparrowdesk-sdk:zipXCFramework -PSDK_VERSION="$VERSION"

XCF_ZIP="sparrowdesk-sdk/build/outputs/SparrowDeskSDK.xcframework.zip"
if [ ! -f "$XCF_ZIP" ]; then
    echo "ERROR: XCFramework zip not found at $XCF_ZIP"
    exit 1
fi

echo "▸ [iOS] Computing checksum..."
CHECKSUM=$(swift package compute-checksum "$XCF_ZIP")
echo "  Checksum: $CHECKSUM"

# ── 3. iOS: Upload to GitHub Releases ─────────────────────

echo ""
echo "▸ [iOS] Creating GitHub Release v${VERSION}..."

TAG="v${VERSION}"

gh release create "$TAG" \
    --repo "$GITHUB_OWNER/$GITHUB_REPO" \
    --title "v${VERSION}" \
    --notes "## SparrowDesk SDK v${VERSION}

### Android (JitPack)

Add the JitPack repository to \`settings.gradle.kts\`:

\`\`\`kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri(\"https://jitpack.io\") }
    }
}
\`\`\`

Then add the dependency in \`build.gradle.kts\`:

\`\`\`kotlin
implementation(\"com.github.${GITHUB_OWNER}.${GITHUB_REPO}:sparrowdesk-sdk:${VERSION}\")
\`\`\`

### iOS (Swift Package Manager)

Add this repo URL in Xcode:
\`\`\`
https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}
\`\`\`
Or add to \`Package.swift\`:
\`\`\`swift
.package(url: \"https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}\", from: \"${VERSION}\")
\`\`\`" \
    "$XCF_ZIP"

echo "✓ GitHub Release created with XCFramework attached"

# ── 4. Update Package.swift ───────────────────────────────

echo ""
echo "▸ Updating Package.swift..."

sed -i '' "s|let version = \".*\"|let version = \"${VERSION}\"|" Package.swift
sed -i '' "s|let checksum = \".*\"|let checksum = \"${CHECKSUM}\"|" Package.swift

echo "✓ Package.swift updated (version=${VERSION}, checksum=${CHECKSUM})"

# ── 5. Summary ────────────────────────────────────────────

echo ""
echo "============================================"
echo "  Published SparrowDesk SDK v${VERSION}"
echo "============================================"
echo ""
echo "  Android (JitPack, served from the git tag):"
echo "    implementation(\"com.github.${GITHUB_OWNER}.${GITHUB_REPO}:sparrowdesk-sdk:${VERSION}\")"
echo ""
echo "  iOS (SPM):"
echo "    .package(url: \"https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}\", from: \"${VERSION}\")"
echo ""
echo "  NEXT STEP: Commit & push the updated Package.swift"
echo "    git add Package.swift"
echo "    git commit -m \"chore: release v${VERSION}\""
echo "    git push origin main"
echo ""
