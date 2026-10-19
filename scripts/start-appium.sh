#!/usr/bin/env bash
# Starts a local Appium server for manual use (e.g. Appium Inspector).
# ANDROID_HOME / JAVA_HOME are set only for this process — the shell profile is not changed.
# Values come from src/test/resources/config/default.properties (same source as AppiumServerManager).
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS="$PROJECT_DIR/src/test/resources/config/default.properties"

prop() {
  local value
  value="$(grep -E "^$1=" "$PROPS" | tail -n 1 | cut -d '=' -f 2- || true)"
  echo "${value/#\~/$HOME}"
}

ANDROID_HOME_VALUE="$(prop appium.env.androidHome)"
ANDROID_HOME_VALUE="${ANDROID_HOME_VALUE:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"

JAVA_HOME_VALUE="$(prop appium.env.javaHome)"
if [[ -z "$JAVA_HOME_VALUE" ]]; then
  JAVA_HOME_VALUE="${JAVA_HOME:-$(/usr/libexec/java_home -v 21 2>/dev/null || true)}"
fi

HOST="$(prop appium.host)"
PORT="$(prop appium.port)"

[[ -d "$ANDROID_HOME_VALUE/platform-tools" ]] || { echo "Android SDK not found at '$ANDROID_HOME_VALUE' (set appium.env.androidHome)" >&2; exit 1; }
[[ -x "$JAVA_HOME_VALUE/bin/java" ]] || { echo "JDK not found at '$JAVA_HOME_VALUE' (set appium.env.javaHome)" >&2; exit 1; }
command -v appium >/dev/null || { echo "appium is not installed (npm install -g appium)" >&2; exit 1; }

LOG_DIR="$PROJECT_DIR/target/artifacts/manual"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/appium-server_$(date +%Y%m%d-%H%M%S).log"

echo "ANDROID_HOME=$ANDROID_HOME_VALUE"
echo "JAVA_HOME=$JAVA_HOME_VALUE"
echo "Appium: http://${HOST:-127.0.0.1}:${PORT:-4723}  log: $LOG_FILE"
echo "Inspector: http://${HOST:-127.0.0.1}:${PORT:-4723}/inspector"

export ANDROID_HOME="$ANDROID_HOME_VALUE"
export JAVA_HOME="$JAVA_HOME_VALUE"
exec appium --address "${HOST:-127.0.0.1}" --port "${PORT:-4723}" --use-plugins=inspector \
  --log "$LOG_FILE" --log-timestamp --local-timezone
