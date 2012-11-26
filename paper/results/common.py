import json
import sys
from collections import OrderedDict

VARNAMES = OrderedDict([
    ('INVPROB_DRAM_FLIP_PER_SECOND', 'DRAM'),
    ('INVPROB_SRAM_READ_UPSET', 'SRAM read'),
    ('INVPROB_SRAM_WRITE_FAILURE', 'SRAM write'),
    ('MB_FLOAT_APPROX', '\\texttt{float} bits'),
    ('MB_DOUBLE_APPROX', '\\texttt{double} bits'),
    ('TIMING_ERROR_PROB_PERCENT-1', 'timing errors: single bit'),
    ('TIMING_ERROR_PROB_PERCENT-2', 'timing errors: random value'),
    ('TIMING_ERROR_PROB_PERCENT-3', 'timing errors: last value'),
])

RSRCNAMES = OrderedDict([
    ('heap', 'DRAM storage'),
    ('stack', 'SRAM storage'),
    ('alu', 'Integer operations'),
    ('fpu', 'FP operations'),
])

LEVELNAMES = ['Mild', 'Medium', 'Aggressive']

BMLONGNAMES = OrderedDict([
    ('SciMark2: FFT', 'FFT'),
    ('SciMark2: SOR', 'SOR'),
    ('SciMark2: MonteCarlo', 'MonteCarlo'),
    ('SciMark2: SMM', 'SparseMatMult'),
    ('SciMark2: LU', 'LU'),
    ('ZXing', 'ZXing'),
    ('jME', 'jMonkeyEngine'),
    ('ImageJ', 'ImageJ'),
    ('Plane', 'Raytracer'),
])

def table_row(cells):
    return ' & '.join(cells) + ' \\\\'

def numstr(f):
    out = '%.2f' % f
    if len(out) > 5:
        return 'lots'
    return out
def percent(f, places=2):
    return ('%.' + str(places) + 'f\\%%') % (f*100)

def benchname(s):
    if ':' in s:
        return s.rsplit(':', 1)[1].strip()
    elif s == 'Plane':
        return 'Raytracer'
    else:
        return s

def json_in():
    return json.load(sys.stdin)

def rtable(table):
    return '\n'.join('\t'.join(str(cell) for cell in row) for row in table)

def frac(a, b):
    total = float(a) + float(b)
    if total == 0.0:
        return 0.0
    else:
        return float(b) / total
