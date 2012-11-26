from __future__ import division
from common import *

import energy_chart

table = energy_chart.table[1:]
savings = []
for row in table:
    name = row.pop(0)
    if name.endswith('-B'):
        continue
    total = sum(float(n) for n in row)
    savings.append((1 - total, name))

savings.sort()
print 'min: %.2f for %s' % savings[0]
print 'max: %.2f for %s' % savings[-1]

for level in (1, 2, 3):
    suffix = '-' + str(level)
    lsavings = [t[0] for t in savings if t[1].endswith(suffix)]
    mean = sum(lsavings) / len(lsavings)
    print 'level %i: %.2f - %.2f; mean %.2f' % \
        (level, lsavings[0], lsavings[-1], mean)
