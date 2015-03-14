all:
	javac $$(find . -name "*.java")
	find src -name "*.class" -exec mv {} bin \;

clean:
	@rm ./bin/*.class 2> /dev/null || true
