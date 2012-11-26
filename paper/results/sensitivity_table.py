from common import *
results = json_in()


# Header row
cells = ['']
cells += VARNAMES.values()
cells.append('Collective')
table = [cells]

# Data rows.
for bmark, _ in BMLONGNAMES.iteritems():
    res = results[bmark]
    cells = [benchname(bmark)]
    for key in VARNAMES:
        cells.append(', '.join(numstr(e) for e in res['individual'][key]))
    cells.append(', '.join(numstr(e) for e in res['collective']))
    table.append(cells)

cols = ' '.join(['c']*(len(table[0])))
print '\\begin{tabular}{ %s }' % cols

for row in table:
    print table_row(row)
# Poor man's matrix transpose.
#for col in xrange(len(table[0])):
#    row = []
#    for i in xrange(len(table)):
#        row.append(table[i][col])
#    print table_row(row)

print '\\end{tabular}'
