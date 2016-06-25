JCC = javac
CP = ./classes/
JFLAGS = -g -d $(CP) -cp lib/weka.jar
JVM = java

SRC = src/Util/Preprocessor.java src/predictor/Predictor.java
				
MAIN = Predictor

all: compile run

compile:
	$(JCC) $(JFLAGS) $(SRC)

run:
	$(JVM) -cp $(CP);lib/weka.jar $(MAIN)
clean:
	del classes\*.class 

