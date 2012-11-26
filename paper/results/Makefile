LATEX=pdflatex -halt-on-error
COMPONENTS=sensitivity_table.tex approximateness_table.tex approximateness_chart.tex sensitivity_chart.tex factor_sensitivity_chart.tex energy_chart.tex applications_table.tex
ST_COMPONENTS=approximations.tex
PYTHON=python

showcase.pdf: showcase.tex $(COMPONENTS) $(ST_COMPONENTS)
	$(LATEX) showcase.tex

sensitivity_table.tex: results.json
	$(PYTHON) sensitivity_table.py < $< > $@

approximateness_table.tex: results.json
	$(PYTHON) approximateness_table.py < $< > $@

approximateness_chart.tex: results.json
	$(PYTHON) approximateness_chart.py < $< | ./plot.r "fraction approximate" > $@

sensitivity_chart.tex: results.json
	$(PYTHON) sensitivity_chart.py < $< | ./plot.r "output error" > $@

factor_sensitivity_chart.tex: results.json
	$(PYTHON) factor_sensitivity_chart.py < $< | ./plot.r "mean output error" > $@

energy_chart.tex: results.json
	$(PYTHON) energy_chart.py < $< | ./stackplot.r "normalized total energy" > $@

applications_table.tex: results.json counts.json
	$(PYTHON) applications_table.py < $< > applications_table.tex

clean:
	rm -f *.aux *.log showcase.pdf $(COMPONENTS)
