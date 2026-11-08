# Assignment 4 — Dynamic Programming and String Algorithms

Nursultan Malgazhdar · SE2516

## Experiment Design Card

| Field | Edit Distance | String search |
| --- | --- | --- |
| Question | How much repeated work does plain recursion perform, and how much storage do two rows save? | How do Naive and KMP scale on random text and on repeated near-matches? |
| Sizes | Both strings have length `n`. Recursion uses 2, 4, 6, 8 to keep the disjoint case feasible. Both DP versions use 128, 256, 512, 1024 to reveal the storage and work trends. | Text lengths 4,096, 8,192, 16,384, 32,768. Pattern length `m = n / 64` grows with the text to expose the worst-case growth of Naive. |
| Inputs | A seeded random string paired with itself, and two seeded random strings over disjoint alphabets. The second input forces all three recursive branches at every non-base call. | A seeded random text and pattern over four symbols, with one copy of the pattern planted in the text. The adversarial text is all `A`, with a pattern of `m-1` copies of `A` followed by `B`, so every Naive alignment fails at the last symbol. |
| Metrics | Recursive calls reveal repeated work. Character comparisons and DP cells track processing. Allocated integer slots compare DP storage. Record median milliseconds per call over five measured trials. | Count text comparisons and LPS comparisons separately and add them for the end-to-end comparison. Record median milliseconds per call over five measured trials. |
| Prediction | The identical pair takes a diagonal recursive path. The disjoint pair grows exponentially. DP fills `n²` interior cells on either input. The full table stores `(n+1)²` integers, while two rows store `2(n+1)`. | Random inputs usually fail early, so both methods are near linear in comparisons. Repeated near-matches make Naive comparisons quadratic as `m` grows with `n`; KMP stays linear. |
| Plots | Calls versus length on a log scale. Allocated integer slots versus length for both DP variants, also on a log scale. | Total comparisons versus text length on a log scale, showing both input types for Naive and KMP. |

The generator uses the fixed seed `42`. Warm up each case with three batches before recording five batches. Batch sizes are fixed by case to reduce clock noise on short calls. Each CSV row represents one measured batch and reports the mean time and exact counters per algorithm call. The report uses the median of the five recorded times.
