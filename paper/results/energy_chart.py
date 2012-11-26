from __future__ import division
from common import *
import sys
results = json_in()

# Constants.

SAVE_FPU_WIDTH = (0.32, 0.78, 0.85)
SAVE_SRAM = (0.70, 0.80, 0.90)
SAVE_DRAM = (0.17, 0.22, 0.24)
SAVE_FUNCTIONAL_V = (0.12, 0.22, 0.30)

PROP_CPU_CACHE = 0.12
PROP_CPU_REG = 0.23
PROP_CPU_INT = 0.15
PROP_CPU_FP = 0.8

PROP_MEMORY = 0.45 # for server; 0.25 on mobile

UNITS_LOAD_STORE = 22 #+ 12 double-counted, cache is already accounted in the SRAM component
UNITS_LOAD_STORE_APPROX = 12 # based on the ISPLED paper, about the same as a register
UNITS_INT_FIXED = 22
UNITS_INT_APPROX = 15
UNITS_FP_FIXED = 22
UNITS_FP_APPROX = 18


table = []

# Header row.
table.append(['', 'DRAM', 'SRAM', 'Integer', 'FP'])

# Data rows.
for bmark, _ in BMLONGNAMES.iteritems():
    res = results[bmark]
    
    approx = res['approximateness']
    
    
    prop_sram = (1 - PROP_MEMORY) * (PROP_CPU_CACHE + PROP_CPU_REG)
    sram_prop_base = prop_sram
    
    dram_prop_base = PROP_MEMORY
    
    int_units_base = sum(approx['alu']) * \
                     (UNITS_INT_FIXED + UNITS_INT_APPROX)
    fp_units_base = sum(approx['fpu']) * \
                    (UNITS_FP_FIXED + UNITS_FP_APPROX)
    
    instr_units_base = int_units_base + fp_units_base
    prop_instr = (1 - prop_sram - PROP_MEMORY)
    int_prop_base = int_units_base / instr_units_base * prop_instr
    fp_prop_base = fp_units_base / instr_units_base * prop_instr
    
    row = [benchname(bmark) + '-B']
    row += (dram_prop_base, sram_prop_base, 
            int_prop_base, fp_prop_base)
    table.append(row)
    
    for level in (0, 1, 2):
        row = [benchname(bmark) + '-' + str(level+1)]
        
        int_units_opt = \
            approx['alu'][0] * (UNITS_INT_FIXED + UNITS_INT_APPROX) + \
            approx['alu'][1] * UNITS_INT_FIXED + \
            approx['alu'][1] * UNITS_INT_APPROX * \
                (1 - SAVE_FUNCTIONAL_V[level])
        fp_units_opt = \
            approx['fpu'][0] * (UNITS_FP_FIXED + UNITS_FP_APPROX) + \
            approx['fpu'][1] * UNITS_FP_FIXED + \
            approx['fpu'][1] * UNITS_FP_APPROX * \
                (1 - SAVE_FPU_WIDTH[level]) * (1 - SAVE_FUNCTIONAL_V[level])
        if int_units_base == 0:
            int_prop_opt = 0.0
        else:
            int_prop_opt = (int_units_opt / int_units_base) * int_prop_base
        if fp_units_base == 0:
            fp_prop_opt = 0.0
        else:
            fp_prop_opt = (fp_units_opt / fp_units_base) * fp_prop_base
    
        sram_prop_opt = (1 - frac(*approx['stack']) * SAVE_SRAM[level]) * \
            sram_prop_base
        dram_prop_opt = (1 - frac(*approx['heap']) * SAVE_DRAM[level]) * \
            dram_prop_base
    
        row += (dram_prop_opt, sram_prop_opt, 
                int_prop_opt, fp_prop_opt)
    
        table.append(row)
    
    # print >>sys.stderr, bmark
    # print >>sys.stderr, int_units_saved, fp_units_saved, loadstore_units, \
    #                     int_units_base, fp_units_base
    # print >>sys.stderr, (int_units_saved + fp_units_saved)
    # print >>sys.stderr, (loadstore_units + int_units_base + fp_units_base)
    # print >>sys.stderr, instr_prop_saved
    # print >>sys.stderr, save_cpu
    # print >>sys.stderr, save_dram, frac(*approx['heap'])

# Turn into a R-legible data file.
print rtable(table)
