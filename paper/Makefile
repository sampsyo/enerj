# Put user-specific changes in your own Makefile.user.
# Make will silently continue if that file does not exist.
-include Makefile.user

TEXINPUTS=.:
LATEX?=pdflatex -halt-on-error

COMPONENTS=results/approximations.tex results/approximateness_chart.tex results/sensitivity_chart.tex results/energy_chart.tex results/applications_table.tex figs/hwmodel.pdf

all: epaj.pdf


%.pdf: $(addsuffix .tex, %) $(addsuffix .bib, %) $(COMPONENTS)
	$(LATEX) $<
	-bibtex $*
	$(LATEX) $<
	$(LATEX) $<
	while grep "Rerun to get" $*.log; do $(LATEX) $< ; done

clean:
	- find . -name "*.aux" | xargs rm
	- find . -name "*.log" | xargs rm
	- find . -name "*.bbl" | xargs rm
	- find . -name "*.blg" | xargs rm

show:
	echo $(USER)
	echo $(LATEX)
	
OTTALL = epaj_common.ott  epaj_st.ott epaj_st_wf.ott epaj_rt.ott epaj_rt_wf.ott

ott_epaj_sysdef.tmp: $(OTTALL)
	ott -sort false \
          -writesys $@ \
          $(OTTALL)

ott_epaj.tex: ott_epaj_sysdef.tmp $(OTTALL)
	ott -sort false -readsys ott_epaj_sysdef.tmp \
	    -o ott_epaj.tex

ott_epaj.pdf: ott_epaj.tex
	$(LATEX) $<
	$(LATEX) $<

ott_epaj_proofs.tex: ott_epaj_sysdef.tmp $(OTTALL) epaj_proofs.tex
	ott  -tex_show_categories true -tex_wrap false -sort false \
          -o ott_epaj_include_all.tex \
	  -tex_filter epaj_proofs.tex ott_epaj_proofs.tex \
	  -readsys ott_epaj_sysdef.tmp
	cat ott_epaj_include_all.tex | sed -e "s/.ottjudgement.ottinterrule//" -e "s/ottuserXXsyntax.ottafterlastrule/ottafterlastrule/" > ott_epaj_include.tex

ott_epaj_proofs.pdf: ott_epaj_proofs.tex techreport.bib
	$(LATEX) $<
	bibtex ott_epaj_proofs
	$(LATEX) $<
	$(LATEX) $<

ott: ott_epaj_proofs.pdf ott_epaj.pdf

# For making results components.
$(COMPONENTS):
	$(MAKE) -C results
