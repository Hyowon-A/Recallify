#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/benchmark_generation.sh --mode <mcq|flashcard> --token <jwt> --set-id <id> --pdf <path> [options]

Required:
  --mode         Endpoint mode: mcq or flashcard
  --token        JWT access token
  --set-id       Target set id
  --pdf          Path to local PDF file

Optional:
  --base-url     API base URL (default: http://localhost:8080)
  --count        Number of cards/questions requested per API call (default: 20)
  --level        Difficulty level (default: medium)
  --requests     Number of repeated requests for this benchmark run (default: 5)
  --tag          Label added to output filename (default: baseline)
  --out-dir      Output directory for result files (default: benchmarks/results)
  --help         Show this help text

Example:
  scripts/benchmark_generation.sh \
    --mode mcq \
    --token "$TOKEN" \
    --set-id 12 \
    --pdf ./sample.pdf \
    --count 20 \
    --level medium \
    --requests 5 \
    --tag before-parallel
EOF
}

MODE=""
TOKEN=""
SET_ID=""
PDF_FILE=""
BASE_URL="http://localhost:8080"
COUNT="20"
LEVEL="medium"
REQUESTS="5"
TAG="baseline"
OUT_DIR="benchmarks/results"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode) MODE="${2:-}"; shift 2 ;;
    --token) TOKEN="${2:-}"; shift 2 ;;
    --set-id) SET_ID="${2:-}"; shift 2 ;;
    --pdf) PDF_FILE="${2:-}"; shift 2 ;;
    --base-url) BASE_URL="${2:-}"; shift 2 ;;
    --count) COUNT="${2:-}"; shift 2 ;;
    --level) LEVEL="${2:-}"; shift 2 ;;
    --requests) REQUESTS="${2:-}"; shift 2 ;;
    --tag) TAG="${2:-}"; shift 2 ;;
    --out-dir) OUT_DIR="${2:-}"; shift 2 ;;
    --help|-h) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

if [[ -z "$MODE" || -z "$TOKEN" || -z "$SET_ID" || -z "$PDF_FILE" ]]; then
  echo "Missing required arguments." >&2
  usage
  exit 1
fi

if [[ "$MODE" != "mcq" && "$MODE" != "flashcard" ]]; then
  echo "--mode must be one of: mcq, flashcard" >&2
  exit 1
fi

if [[ ! -f "$PDF_FILE" ]]; then
  echo "PDF not found: $PDF_FILE" >&2
  exit 1
fi

if ! [[ "$REQUESTS" =~ ^[0-9]+$ ]] || [[ "$REQUESTS" -lt 1 ]]; then
  echo "--requests must be an integer >= 1" >&2
  exit 1
fi

mkdir -p "$OUT_DIR"

if [[ "$MODE" == "mcq" ]]; then
  ENDPOINT="/api/mcq/generate-from-pdf"
else
  ENDPOINT="/api/flashcard/generate-from-pdf"
fi

URL="${BASE_URL%/}${ENDPOINT}"
TS="$(date -u +%Y%m%dT%H%M%SZ)"
RUN_ID="${TS}_${MODE}_${TAG}"
RAW_CSV="${OUT_DIR}/${RUN_ID}.csv"
SUMMARY_TXT="${OUT_DIR}/${RUN_ID}.summary.txt"

GIT_COMMIT="$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"

echo "iteration,http_status,time_seconds" > "$RAW_CSV"
echo "Running ${REQUESTS} request(s) against ${URL}"

for i in $(seq 1 "$REQUESTS"); do
  TMP_BODY="$(mktemp)"
  METRICS="$(
    curl -sS \
      -o "$TMP_BODY" \
      -w "%{http_code},%{time_total}" \
      -X POST "$URL" \
      -H "Authorization: Bearer ${TOKEN}" \
      -F "setId=${SET_ID}" \
      -F "count=${COUNT}" \
      -F "level=${LEVEL}" \
      -F "file=@${PDF_FILE};type=application/pdf"
  )"
  rm -f "$TMP_BODY"

  HTTP_STATUS="${METRICS%%,*}"
  TIME_SECONDS="${METRICS##*,}"
  echo "${i},${HTTP_STATUS},${TIME_SECONDS}" >> "$RAW_CSV"
  echo "  #${i}: status=${HTTP_STATUS} time=${TIME_SECONDS}s"
done

SUCCESS_COUNT="$(awk -F, 'NR>1 && $2 ~ /^2[0-9][0-9]$/ {c++} END {print c+0}' "$RAW_CSV")"
TOTAL_COUNT="$REQUESTS"

TIME_TMP="$(mktemp)"
awk -F, 'NR>1 {print $3}' "$RAW_CSV" | sort -n > "$TIME_TMP"
N="$(wc -l < "$TIME_TMP" | tr -d ' ')"

if [[ "$N" -eq 0 ]]; then
  echo "No timing samples captured." >&2
  rm -f "$TIME_TMP"
  exit 1
fi

AVG="$(awk '{s+=$1} END {printf "%.6f", s/NR}' "$TIME_TMP")"
MIN="$(sed -n '1p' "$TIME_TMP")"
MAX="$(sed -n "${N}p" "$TIME_TMP")"
P50_IDX="$(( (50 * N + 99) / 100 ))"
P95_IDX="$(( (95 * N + 99) / 100 ))"
P50="$(sed -n "${P50_IDX}p" "$TIME_TMP")"
P95="$(sed -n "${P95_IDX}p" "$TIME_TMP")"
rm -f "$TIME_TMP"

{
  echo "run_id: ${RUN_ID}"
  echo "timestamp_utc: ${TS}"
  echo "git_commit: ${GIT_COMMIT}"
  echo "mode: ${MODE}"
  echo "url: ${URL}"
  echo "set_id: ${SET_ID}"
  echo "count_per_request: ${COUNT}"
  echo "level: ${LEVEL}"
  echo "requests: ${TOTAL_COUNT}"
  echo "success_2xx: ${SUCCESS_COUNT}"
  echo "avg_seconds: ${AVG}"
  echo "p50_seconds: ${P50}"
  echo "p95_seconds: ${P95}"
  echo "min_seconds: ${MIN}"
  echo "max_seconds: ${MAX}"
  echo "raw_csv: ${RAW_CSV}"
} | tee "$SUMMARY_TXT"

echo "Saved summary to ${SUMMARY_TXT}"
