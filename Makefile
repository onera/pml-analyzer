# Paths
BUILDDIR="build"
EXPORTDIR="export"
ANALYSISDIR="analysis"

# Commands
BIBTEX=bibtex
MAKEINDEX=makeindex -s nomencl.ist 
LATEX = pdflatex -interaction=nonstopmode --output-directory=$(BUILDDIR)
DOT=dot -Tpdf


# Compilable files
FILENAMES=$(shell grep -l -e \begin{document} $(EXPORTDIR)/*.tex)
REPORTNAMES=$(shell for file in $(FILENAMES); do basename $$file .tex; done;)
PMLNAMES=$(shell ls $(EXPORTDIR)/*.dot)
PMLBASENAME=$(shell for file in $(PMLNAMES); do basename $$file .dot; done;)
ANALYSESNAMES=$(shell grep -l -e \< $(ANALYSISDIR)/*.txt)

# Compiling the patterns (TEX files) and the PML exports (DOT files)
all: patterns pml

sortResultFiles:
	@for file in $(ANALYSESNAMES); do \
    		echo "Sorting $$file"; \
    		sort -o "$$file" "$$file"; \
    done

# Compiling all DOT files in the directory
pml:
	@for file in $(PMLBASENAME); do \
		echo "Compiling $$file"; \
		$(DOT)  -o "$(EXPORTDIR)/$$file.pdf" "$(EXPORTDIR)/$$file.dot"; \
	done

# Compiling all TEX files in the directory
patterns: mkDir latex move cleanBuildDir

# Export the code as a TAR archive
compress:
	@tar -cf pml.tar src/ lib/ README.md tikzStyle.tex build.sbt Makefile AUTHORS.txt COPYING.txt

# Export only pattern code as TAR archive
compressPatterns:
	@tar -cf pattern.tar --exclude='src/main/scala/views/patterns/examples/ClairePattern.scala' --exclude='src/main/scala/views/patterns/examples/PhylogPatternsInstances.scala' src/main/scala/views/patterns README.md tikzStyle.tex build.sbt Makefile AUTHORS.txt COPYING.txt

# Compiling all TEX files
latex:
	@for file in $(REPORTNAMES); do \
		echo "Compiling $$file"; \
		$(LATEX) "\newcommand{\standalone}{} \input{$(EXPORTDIR)/$$file}" >/dev/null; \
		egrep -i "Latex Error" "$(BUILDDIR)/$$file".log ||  echo "Success" ; \
	done

# Transforming all PDF to PNG
png:
	@for file in $(REPORTNAMES); do \
		echo "Transforming $$file"; \
		convert -trim  $(EXPORTDIR)/"$$file.pdf" -quality 100 $(EXPORTDIR)/"$$file.png"; \
	done

# Extract TEX compilation results from the temporary build directory
move: 
	@mv $(BUILDDIR)/*.pdf $(EXPORTDIR)

# Create the temporary build directory
mkDir: 
	@mkdir -p build

# Remove the build directory
cleanBuildDir:
	@ rm -rf $(BUILDDIR) 

# remove all PDF compiled out of the TEX
clean: cleanBuildDir
	@rm -rf $(EXPORTDIR) $(ANALYSISDIR)

.PHONY: clean
