from common import *
results = json_in()

table = []

# Header row.
table.append([''] + RSRCNAMES.values())

# Data rows.
for bmark, _ in BMLONGNAMES.iteritems():
    res = results[bmark]
    row = [benchname(bmark)]
    for key in RSRCNAMES:
        a, p = res['approximateness'][key]
        row.append(frac(a, p))
    table.append(row)

# Turn into a R-legible data file.
print rtable(table)
