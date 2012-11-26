The Python scripts in this directory require Python 2.7.

The R plotting scripts use the tikzDevice R package to produce TikZ
(LaTeX-compatible) code. To get it, run this in R:

> install.packages("tikzDevice", dependencies=T)

To use the figures, you'll need the TikZ LaTeX package. This is part of
TeXLive's "texlive-pictures" package. If you use MacPorts, then you can get it
by running:

$ sudo port install texlive-pictures

The command is probably pretty similar for other package managers. And, on a
sidenote, the "supertabular" LaTeX package used by the semantics stuff is in the
"texlive-latex-extra" package:

$ sudo port install texlive-latex-extra
