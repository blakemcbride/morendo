# Builds morendo.pdf from morendo.tex and chapters/*.tex.
# Requires a TeX Live installation with pdflatex (listings, booktabs, hyperref,
# framed, upquote, enumitem, microtype, lmodern are all standard TeX Live packages).

MAIN   = morendo
TEX    = pdflatex -interaction=nonstopmode -halt-on-error
SRC    = $(MAIN).tex $(wildcard chapters/*.tex)

all: $(MAIN).pdf

# Three passes: table of contents and cross references need two, hyperref a third.
$(MAIN).pdf: $(SRC)
	$(TEX) $(MAIN).tex
	$(TEX) $(MAIN).tex
	$(TEX) $(MAIN).tex

clean:
	rm -f $(MAIN).aux $(MAIN).log $(MAIN).out $(MAIN).toc $(MAIN).lof $(MAIN).lot \
	      chapters/*.aux

realclean: clean
	rm -f $(MAIN).pdf

.PHONY: all clean realclean
