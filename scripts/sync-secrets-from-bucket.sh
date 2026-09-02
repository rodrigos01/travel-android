#!/bin/bash
# Mirrors this repo's secrets from the shared GCS bucket onto the local
# checkout. Shared between the Claude Cloud environment's SessionStart hook
# and CI - callers are responsible for authentication; this script only
# needs a valid OAuth access token, however it was obtained (JWT-bearer
# exchange with a static service-account key, Workload Identity Federation,
# `gcloud auth print-access-token`, ...).
#
# The bucket mirrors, under <github-owner>/<repo>/, exactly the file tree
# that needs to land at the repo root - e.g. local.properties and
# app/release.jks for this project. Every object under that prefix is
# downloaded to the same relative path, so a project stages whatever it
# needs without this script knowing anything project-specific.
#
# Required env vars:
#   GCS_SECRETS_BUCKET   bucket name (no gs:// prefix)
#   GCS_ACCESS_TOKEN     bearer token with storage.objectViewer on the bucket
# Optional env vars:
#   REPO_PATH             "<owner>/<repo>" bucket prefix; auto-derived from
#                          `git remote get-url origin` if unset (CI should
#                          just pass REPO_PATH="${{ github.repository }}")
#   DEST_DIR               where to write files; defaults to the current
#                          directory
set -euo pipefail

: "${GCS_SECRETS_BUCKET:?GCS_SECRETS_BUCKET is required}"
: "${GCS_ACCESS_TOKEN:?GCS_ACCESS_TOKEN is required}"
DEST_DIR="${DEST_DIR:-$(pwd)}"

if [ -z "${REPO_PATH:-}" ]; then
  remote_url=$(git -C "$DEST_DIR" remote get-url origin 2>/dev/null) || {
    echo "REPO_PATH not set and no git remote to derive it from" >&2; exit 1; }
  REPO_PATH=$(echo "$remote_url" | sed -E 's#^(git@|https://)([^:/]+)[:/]##; s#\.git$##')
fi
[ -n "$REPO_PATH" ] || { echo "could not determine REPO_PATH" >&2; exit 1; }

prefix="${REPO_PATH}/"
objects=$(curl -fsS -H "Authorization: Bearer $GCS_ACCESS_TOKEN" \
  "https://storage.googleapis.com/storage/v1/b/${GCS_SECRETS_BUCKET}/o?prefix=$(jq -rn --arg v "$prefix" '$v|@uri')" \
  | jq -r '.items[]?.name // empty')

synced=0
while IFS= read -r obj; do
  [ -z "$obj" ] && continue
  [[ "$obj" == */ ]] && continue   # GCS "directory marker" placeholder objects
  rel="${obj#$prefix}"
  dest="$DEST_DIR/$rel"
  mkdir -p "$(dirname "$dest")"
  curl -fsS -H "Authorization: Bearer $GCS_ACCESS_TOKEN" \
    "https://storage.googleapis.com/storage/v1/b/${GCS_SECRETS_BUCKET}/o/$(jq -rn --arg v "$obj" '$v|@uri')?alt=media" \
    -o "$dest"
  echo "  synced $rel"
  synced=$((synced + 1))
done <<< "$objects"

if [ "$synced" -eq 0 ]; then
  echo "nothing staged for gs://${GCS_SECRETS_BUCKET}/${prefix}" >&2
  exit 1
fi