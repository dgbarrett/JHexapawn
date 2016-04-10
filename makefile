SRC_LIST_EXT = .sourcelist
SRC_LIST = src$(SRC_LIST_EXT)
JAVA_VERSION = 1.6
MAIN = Hexapawn


.PHONY: run
run: build
	java -cp '.:build' $(MAIN)

.PHONY: build
build: 
	find src -name "*.java" > $(SRC_LIST)
	javac -d build @$(SRC_LIST)
	rm $(SRC_LIST)

.PHONY: clean
clean:
	rm -rf build/*/*.class
	rm -rf build/*
	rm -f *.sourcelist
