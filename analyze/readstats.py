#!/usr/bin/env python

import sys
import json

DEFAULT_FILENAME = 'enerjstats.json'

def showstatblock(items, unit='ops'):
    for name, (precise, approx) in items:
        total = precise + approx
        frac = float(approx) / total
        print '  %s: %i %s total, %.1f%% approx' % \
                    (name, total, unit, frac*100)

def showstats(stats):
    ops_arith = []
    ops_mem = []
    for name, vals in stats['operations'].iteritems():
        if name.startswith('load') or name.startswith('store'):
            ops_mem.append((name, vals))
        else:
            ops_arith.append((name, vals))
    ops_mem.sort()
    ops_arith.sort()
    
    footprint = []
    for name, vals in stats['footprint'].iteritems():
        section, kind = name.split('-')
        if kind != 'bytes':
            # Skip object stats -- they're not very useful.
            continue
        footprint.append((section, vals))
    footprint.sort()
    
    print 'Arithmetic operations:'
    showstatblock(ops_arith, 'ops')
    print
    print 'Memory accesses:'
    showstatblock(ops_mem, 'accesses')
    print
    print 'Footprint:'
    showstatblock(footprint, 'byte-ms')

if __name__ == '__main__':
    args = sys.argv[1:]
    if args:
        files = args
    else:
        files = [DEFAULT_FILENAME]
    
    for fn in files:
        with open(fn) as f:
            stats = json.load(f)
        showstats(stats)
