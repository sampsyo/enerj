from __future__ import division
from common import *
res = json_in()
counts = json.load(open('counts.json'))

print r"""
\begin{tabular}{l >{\raggedright}p{1.35in} >{\raggedright}p{1.25in} r r r r r}
& & & \multicolumn{1}{c}{Lines} & \multicolumn{1}{c}{Proportion} &
\multicolumn{1}{c}{Total} & \multicolumn{1}{c}{Annotated} & \tabularnewline
Application & Description & Error metric &
\multicolumn{1}{c}{of code} & \multicolumn{1}{c}{FP} &
\multicolumn{1}{c}{decls.}
& \multicolumn{1}{c}{decls.} & Endorsements \tabularnewline
\hline"""

table_base = [
    ('FFT', r"FFT & \multirow{5}{1.5in}{Scientific kernels from the SciMark2 benchmark} & Mean entry difference", False),
    ('SOR', r"SOR & & Mean entry difference", False),
    ('MonteCarlo', r"MonteCarlo & & Normalized difference", False),
    ('SMM', r"SparseMatMult & & Mean normalized difference", False),
    ('LU', r"LU & & Mean entry difference", True),
    
    ('ZXing', r"ZXing & Smartphone bar code decoder & 1 if incorrect, 0 if correct", False),
    
    ('jME', r"jMonkeyEngine & Mobile/desktop game engine & Fraction of correct decisions normalized to 0.5", False),
    
    ('ImageJ', r"ImageJ & Raster image manipulation & Mean pixel difference", False),
    
    ('Plane', r"Raytracer & 3D image renderer & Mean pixel difference", False),
]

for name, row, skip in table_base:
    print row,
    
    count = counts[name]
    if name not in res:
        name = 'SciMark2: ' + name # hack!
    approx = res[name]['approximateness']
    print ' & %i & %s & %i & %s & %i' % (
        count['loc'],
        percent(frac(sum(approx['alu']), sum(approx['fpu'])), 1),
        count['locations'],
        percent(count['annotations'] / count['locations'], 0),
        count['endorsements'],
    ),
    
    if skip:
        print r'\tabularnewline[1.5ex]'
    else:
        print r'\tabularnewline'
        
print r"\end{tabular}"
