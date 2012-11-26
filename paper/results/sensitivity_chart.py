from common import *
import math
results = json_in()

table = []

# Header row.
table.append([''] + LEVELNAMES)

# Data rows.
for bmark, _ in BMLONGNAMES.iteritems():
    res = results[bmark]
    row = [benchname(bmark)]
    for error in res['collective']:
        if math.isnan(error):
            row.append(1.0)
        else:
            row.append(error)
    table.append(row)

# Turn into a R-legible data file.
print rtable(table)
