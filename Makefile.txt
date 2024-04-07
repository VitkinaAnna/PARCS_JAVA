all: run

clean:
	rm -f out/Hash.jar

out/Hash.jar: out/parcs.jar src/Hash.java
	@mkdir -p temp
	@javac -cp out/parcs.jar -d temp src/Hash.java
	@jar cf out/Hash.jar -C temp .
	@rm -rf temp/

build: out/Hash.jar

run: out/Hash.jar
	@cd out && java -cp 'parcs.jar:Hash.jar' Hash $(WORKERS)
