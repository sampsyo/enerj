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
par(xpd=T, mar=c(4.2,3.9,1.5,0))

labels <- colnames(table)
# labels <- sapply(labels, function(x) gsub(" ", "\\\\n", x))

colors <- gray.colors(dim(table)[1])
bars <- barplot(table, beside=F, col=colors, ylim=c(0.0, 1.0),
				axisnames=F, axes=F)
mtext(ylabel, side=2, line=3.3)

percents <- paste(seq(0, 100, 20), "\\%", sep="")
axis(2, at=seq(0,1,0.2), las=2, labels=percents)

# Get rid of dots in row names.
categories <- row.names(table)
categories <- sapply(categories, function(x) gsub("\\.", " ", x))
# leftpos <- (dim(table)[2]+1)*dim(table)[1]+3
legend(-8, 1.28, categories, bty="n", fill=colors, horiz=T)

#axis(1, lab=F)
names <- colnames(table)
letterlabels <- substr(names, nchar(names), nchar(names))
text(bars + 0.3, par("usr")[3] - 0.05, labels=letterlabels,
     adj=0.9, cex=0.65)
#text(axTicks(1) + 0.5, par("usr")[3] - 0.1, srt=45, adj=1,
#          labels=labels, xpd=T)

reprskip <- 4
reprnames <- window(names, deltat=reprskip)
reprnames <- substr(reprnames, 1, nchar(reprnames) - 2)
# reprnames <- c("I", "mmm")
reprpos <- window(bars, start=3, deltat=reprskip)
# text(reprpos - 0.3, par("usr")[3] - 0.2, labels=reprnames)
text(reprpos + 0.6, par("usr")[3] - 0.16, labels=reprnames, srt=45, adj=c(1.0, 0.0))

dummy <- dev.off() # suppress "null device 1"
