from common import *
import math
results = json_in()

table = []

# Header row.
table.append([''] + LEVELNAMES)

# Data rows.
for var, varname in VARNAMES.iteritems():
    totalerrs = [0.0, 0.0, 0.0]
    for bmark, res in results.iteritems():
        errs = res['individual'][var]
        for i in xrange(3):
            totalerrs[i] += errs[i]
    for i in xrange(3):
        totalerrs[i] /= len(results) # Average (over all benchmarks).
    if ': ' in varname:
        varname = varname.split(':')[1].strip()
    table.append([varname] + totalerrs)

# Turn into a R-legible data file.
print rtable(table)
