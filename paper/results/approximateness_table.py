from common import *
results = json_in()

cols = ' '.join(['c']*(len(RSRCNAMES) + 1))
print '\\begin{tabular}{ %s }' % cols

# Header row
cells = ['']
cells += RSRCNAMES.values()
print table_row(cells)

# Data rows.
for bmark, _ in BMLONGNAMES.iteritems():
    res = results[bmark]
    cells = [benchname(bmark)]
    for key in RSRCNAMES:
        a, p = res['approximateness'][key]
        cells.append(percent(frac(a, p)))
    print table_row(cells)

print '\\end{tabular}'
