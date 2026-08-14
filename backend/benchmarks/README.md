# Performance Baseline Workflow

Use this before and after optimization so results are comparable.

## 1) Start backend normally

Run the same environment for both baseline and post-change runs:

- same machine
- same DB
- same `GEMINI_API_KEY`
- same dataset / set ID
- same PDF file
- same `count`, `level`, and number of repeated requests

## 2) Get an access token

Call login/register and copy `accessToken`.

## 3) Run baseline benchmark

MCQ:

```bash
scripts/benchmark_generation.sh \
  --mode mcq \
  --token "$TOKEN" \
  --set-id 123 \
  --pdf ./benchmarks/sample.pdf \
  --count 20 \
  --level medium \
  --requests 5 \
  --tag before-parallel
```

Flashcards:

```bash
scripts/benchmark_generation.sh \
  --mode flashcard \
  --token "$TOKEN" \
  --set-id 123 \
  --pdf ./benchmarks/sample.pdf \
  --count 20 \
  --level medium \
  --requests 5 \
  --tag before-parallel
```

Outputs are written to `benchmarks/results/`:

- `*.csv` raw per-request timings
- `*.summary.txt` aggregate metrics (`avg`, `p50`, `p95`, `min`, `max`, success count)

## 4) Repeat after optimization

Run the same commands with a different tag, for example `--tag after-parallel`.

Then compare the two summary files in `benchmarks/results/`.
