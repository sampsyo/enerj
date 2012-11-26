#!/usr/bin/env Rscript
args <- commandArgs(trailingOnly=T)
ylabel <- args[1]

table <- t(as.matrix(
	read.table(file("stdin"), sep='\t', header=T, row.names=1)
))

require("tikzDevice")
options(tikzMetricsDictionary = "tikzdict") # cache TeX measurements
tikz(console=T, standAlone=F, width=3.3, height=2.1) # send TeX code to stdout

# Expand right side of clipping rect to make room for the legend. Eliminate
# other margins.
par(xpd=T, mar=c(3.55,3,2.6,0))

labels <- colnames(table)
# labels <- sapply(labels, function(x) gsub(" ", "\\\\n", x))

colors <- gray.colors(dim(table)[1])
bars <- barplot(table, beside=T, col=colors, ylim=c(0.0, 1.0),
				axisnames=F, axes=F)
axis(2, at=seq(0,1,0.2), las=2)
mtext(ylabel, side=2, line=2.4)

# Get rid of dots in row names.
categories <- row.names(table)
categories <- sapply(categories, function(x) gsub("\\.", " ", x))
# leftpos <- (dim(table)[2]+1)*dim(table)[1]+3
if (length(categories) <= 3) {
	ncol <- length(categories)
	lposy <- 1.28
	lposx <- -7
} else {
	ncol <- 2
	lposy <- 1.41
	lposx <- -5
}
legend(lposx, lposy, categories, bty="n", fill=colors, ncol=ncol)

#axis(1, lab=F)
text(bars[2,], par("usr")[3] - 0.1, labels=labels, srt=45, adj=0.9)
#text(axTicks(1) + 0.5, par("usr")[3] - 0.1, srt=45, adj=1,
#          labels=labels, xpd=T)

dummy <- dev.off() # suppress "null device 1"
