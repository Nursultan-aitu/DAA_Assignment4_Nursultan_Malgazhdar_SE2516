# DAA Assignment 4 · Dynamic Programming and String Algorithms

Nursultan Malgazhdar · SE2516

The project compares three implementations of Levenshtein edit distance and two algorithms for finding every occurrence of a pattern in a text.

## Run

Use JDK 17 or newer. The Maven Wrapper uses Maven 3.9.9. Its first use needs access to Maven Central unless the distribution and dependencies are already cached.

| Task | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Build and test | `./mvnw.cmd clean verify` | `./mvnw clean verify` |
| Run the experiment after building | `java -cp target/classes edu.daa.benchmark.Benchmark` | `java -cp target/classes edu.daa.benchmark.Benchmark` |
| Rebuild the PNG plots | `python tools/plot_results.py` | `python3 tools/plot_results.py` |

Plot regeneration uses Python 3 with Pillow (`python -m pip install -r tools/requirements.txt`). Run commands from the repository root. The benchmark writes `results/results.csv` by default and accepts an optional alternative output path. The CSV and plots included in this project come from an actual run.

Edit distance uses unit insertion, deletion, and replacement costs. Search returns ascending, zero-based positions, including overlaps. An empty pattern raises `IllegalArgumentException`. A pattern longer than its text produces an empty list.

Read [REPORT.md](REPORT.md) for the experiment design, plots and results. The fixed seed reproduces inputs, answers and operation counts. Timings depend on the environment and vary between runs.

## AI use

OpenAI Codex assisted with the Java implementations, JUnit tests, experiment code, plots, and report drafting. The report describes the measurements recorded by running the included benchmark.
