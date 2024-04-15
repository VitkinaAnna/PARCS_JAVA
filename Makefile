all: run

clean:
	rm -f out/QuickHull.jar

out/QuickHull.jar: out/parcs.jar src/QuickHull.java
	@mkdir -p temp
	@javac -cp out/parcs.jar -d temp src/QuickHull.java
	@jar cf out/QuickHull.jar -C temp .
	@rm -rf temp/

build: out/QuickHull.jar

run: out/QuickHull.jar
	@cd out && java -cp 'parcs.jar:QuickHull.jar' QuickHull $(WORKERS)
