"""Render the report figures directly from the recorded benchmark CSV."""

import csv
import math
import statistics
from collections import defaultdict
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
PLOTS = ROOT / "results" / "plots"
WIDTH, HEIGHT = 1220, 575
LEFT, TOP, RIGHT, BOTTOM = 152, 113, 1160, 434
INK, MUTED, GRID = "#172b3a", "#465d6e", "#dce5eb"
COLORS = ("#236a9c", "#b65724", "#137d69", "#7556a5")


def font(size, bold=False):
    candidates = [
        "C:/Windows/Fonts/" + ("arialbd.ttf" if bold else "arial.ttf"),
        "/usr/share/fonts/truetype/dejavu/" + ("DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf"),
        "/System/Library/Fonts/Supplemental/" + ("Arial Bold.ttf" if bold else "Arial.ttf"),
    ]
    for name in candidates:
        if Path(name).is_file():
            return ImageFont.truetype(name, size)
    return ImageFont.load_default(size=size)


def load_results():
    groups = defaultdict(list)
    with (ROOT / "results" / "results.csv").open(encoding="utf-8", newline="") as file:
        for row in csv.DictReader(file):
            groups[row["algorithm"], row["input"], int(row["n"])].append(row)
    for key, rows in groups.items():
        if sorted(int(row["trial"]) for row in rows) != [1, 2, 3, 4, 5]:
            raise ValueError(f"Expected five measured trials for {key}")
    return groups


def count_series(groups, algorithm, input_type, columns):
    series = []
    for (name, input_name, n), rows in sorted(groups.items()):
        if name == algorithm and input_name == input_type:
            counts = {sum(int(row[column]) for column in columns) for row in rows}
            if len(counts) != 1:
                raise ValueError(f"Counts changed across trials for {name}, {input_name}, {n}")
            series.append((n, counts.pop()))
    if len(series) < 4:
        raise ValueError(f"Missing sizes for {algorithm}, {input_type}")
    return series


def line_segment(draw, start, end, color, dashed):
    if not dashed:
        draw.line([start, end], fill=color, width=4)
        return
    length = math.dist(start, end)
    for offset in range(0, math.ceil(length), 20):
        lo = offset / length
        hi = min(offset + 11, length) / length
        draw.line([
            (start[0] + (end[0] - start[0]) * lo, start[1] + (end[1] - start[1]) * lo),
            (start[0] + (end[0] - start[0]) * hi, start[1] + (end[1] - start[1]) * hi),
        ], fill=color, width=4)


def draw_plot(filename, title, subtitle, x_label, y_label, series, log_x=False):
    image = Image.new("RGB", (WIDTH, HEIGHT), "white")
    draw = ImageDraw.Draw(image)
    draw.text((43, 28), title, font=font(33, bold=True), fill=INK)
    draw.text((44, 72), subtitle, font=font(19), fill=MUTED)

    x_values = sorted({n for _, points, _ in series for n, _ in points})
    y_values = [value for _, points, _ in series for _, value in points]
    y_min = math.floor(math.log10(min(y_values)))
    y_max = math.ceil(math.log10(max(y_values)))
    transform_x = math.log2 if log_x else float
    x_min, x_max = transform_x(x_values[0]), transform_x(x_values[-1])

    def x_pos(n):
        return LEFT + 24 + (RIGHT - LEFT - 48) * (transform_x(n) - x_min) / (x_max - x_min)

    def y_pos(value):
        return BOTTOM - (BOTTOM - TOP) * (math.log10(value) - y_min) / (y_max - y_min)

    for exponent in range(y_min, y_max + 1):
        tick = 10 ** exponent
        y = y_pos(tick)
        draw.line([(LEFT, y), (RIGHT, y)], fill=GRID, width=1)
        draw.text((LEFT - 12, y), f"{tick:,}", font=font(17), fill=MUTED, anchor="rm")

    draw.line([(LEFT, TOP), (LEFT, BOTTOM), (RIGHT, BOTTOM)], fill="#668092", width=2)
    for value in x_values:
        x = x_pos(value)
        draw.line([(x, BOTTOM), (x, BOTTOM + 6)], fill="#668092", width=2)
        draw.text((x, BOTTOM + 12), f"{value:,}", font=font(17), fill=MUTED, anchor="mt")
    draw.text(((LEFT + RIGHT) / 2, BOTTOM + 43), x_label, font=font(18), fill=INK, anchor="mt")

    label = Image.new("RGBA", (410, 30))
    ImageDraw.Draw(label).text((205, 15), y_label, font=font(18), fill=INK, anchor="mm")
    label = label.rotate(90, expand=True)
    image.paste(label, (15, int((TOP + BOTTOM - label.height) / 2)), label)

    for index, (name, points, dashed) in enumerate(series):
        color = COLORS[index]
        positions = [(x_pos(n), y_pos(value)) for n, value in points]
        for start, end in zip(positions, positions[1:]):
            line_segment(draw, start, end, color, dashed)
        for x, y in positions:
            if dashed:
                draw.rectangle((x - 5, y - 5, x + 5, y + 5), fill="white", outline=color, width=3)
            else:
                draw.ellipse((x - 6, y - 6, x + 6, y + 6), fill=color, outline="white", width=2)
        legend_x = LEFT + index * ((RIGHT - LEFT) // len(series))
        line_segment(draw, (legend_x, 530), (legend_x + 32, 530), color, dashed)
        draw.text((legend_x + 42, 530), name, font=font(17), fill=INK, anchor="lm")

    image.save(PLOTS / filename, optimize=True)


def main():
    groups = load_results()
    PLOTS.mkdir(parents=True, exist_ok=True)
    draw_plot(
        "recursive_calls.png", "Plain recursion revisits subproblems",
        "Identical strings follow one branch. Disjoint alphabets force three branches at every mismatch.",
        "Length of each string (characters)", "Recursive calls per computation (log scale)",
        [(label, count_series(groups, "recursive", input_type, ["recursive_calls"]), dashed)
         for label, input_type, dashed in [("Identical", "identical", True), ("Disjoint alphabets", "disjoint", False)]],
    )
    draw_plot(
        "dp_memory.png", "Two rows keep the DP storage linear",
        "Allocated integer slots for equal-length inputs. Both implementations fill the same number of interior cells.",
        "Length of each string (characters, log scale)", "Allocated int slots per computation (log scale)",
        [(label, count_series(groups, name, "disjoint", ["buffer_ints"]), dashed)
         for label, name, dashed in [("Full table", "table", False), ("Two rows", "two_rows", True)]],
        log_x=True,
    )
    draw_plot(
        "search_comparisons.png", "Repeated near-matches expose Naive search",
        "Pattern length is n / 64. KMP comparisons include both LPS construction and the search.",
        "Text length n (characters, log scale)", "Character comparisons per search (log scale)",
        [(label, count_series(groups, name, input_type, ["search_comparisons", "lps_comparisons"]), dashed)
         for label, name, input_type, dashed in [
             ("Naive · random", "naive", "random_planted", True),
             ("Naive · late mismatch", "naive", "late_mismatch", False),
             ("KMP · random", "kmp", "random_planted", True),
             ("KMP · late mismatch", "kmp", "late_mismatch", False),
         ]],
        log_x=True,
    )
    print("Created 3 PNG charts.")
    for (algorithm, input_type, n), rows in sorted(groups.items()):
        first = rows[0]
        time_ms = statistics.median(float(row["time_ms"]) for row in rows)
        search_total = int(first["search_comparisons"]) + int(first["lps_comparisons"])
        print(f"{algorithm:9} {input_type:14} n={n:5}  median={time_ms:10.6f} ms  "
              f"calls={first['recursive_calls']:>7}  cells={first['dp_cells']:>7}  "
              f"slots={first['buffer_ints']:>7}  search_cmp={search_total:>9}")


if __name__ == "__main__":
    main()
