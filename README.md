# Prelude Constraint Verifier

## Installation

These instructions are for Debian-based Linux distributions. Installation on
MacOS is possible, and installation on Windows _may_ be posssible; but in
either case, some of the steps for will differ.

### Python (for the cvc5 solver build)

There are many ways to install `python`. These instructions assume `pyenv` is
installed, and use it to create and activate a suitable environment for the
build. If you already have an installation, chances are you can just use that.

```
# Create and activate a virtual python environment
pyenv install 3.13.2
pyenv virtualenv 3.13.2 cvc5
pyenv activate cvc5

# Install something needed by make
python -m pip install pyparsing
```

Stay in the same terminal so that the environment will remain active.

### The java virtual machine

A JVM is necessary both for the cvc5 build and to use prelude. If you already
have a java installation, chances are you can just use that.

```
# Install java virtual machine
sudo apt install openjdk-25-jdk

# Tell make where java is (might differ by architecture?)
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
```

Note that the cvc5 build process _does_ require `JAVA_HOME` to be set.

### The cvc5 solver

Someone created an `apt` package for cvc5, but that package on last review lags
far behind development, and is currently incompatible with Prelude. These
instructions are for building cvc5 from source, in particular the version of
cvc5 that is not the most recent, and one that is compatible with prelude.

We have observed though that upstream changes to cvc5 dependencies sometimes
make it so older versions of cvc5 no longer build. This will be a risk for
reproducibility unless someone actively maintains prelude.

```
# Install tooling for the build
sudo apt install cmake m4

# Download the cvc5 repository
git clone https://github.com/cvc5/cvc5

# Switch to a version known to work with Prelude
git checkout cvc5-1.3.1

# Configure the build with some extensions
cd cvc5
./configure.sh production --auto-download --cocoa --gpl --java-bindings

# Build cvc5 (takes a long time)
cd build
make

# Install libraries to /usr/local
sudo make install
```

This should put cvc5 binaries and libraries libraries in `/usr/local`.

### Prelude

```
# Install build tool for the project
sudo apt install maven

# Download the prelude repospitory
git clone ...

# Tell java to use the cvc5 libraries (mac os only)
export DYLD_LIBRARY_PATH=/usr/local/lib

# Tell java to use the cvc5 libraries (linux only)
export LD_LIBRARY_PATH=/usr/local/lib

# Build prelude
cd prelude
mvn clean package
```

## Running examples

The tool accepts as parameters a field size with the switch `--field-size=x` or
`-s x` and the path to a prelude source file. To run the tool on an example
using the binary field, try

```
java -jar target/prelude.jar --field-size=2 example/gates-passive.txt
```

The tool outputs a list of the functions in the source file. It may output
additional logging along the way for diagnosis and tracking progress. Each
function is labeled

- **PASS** If the function satisfies the contract specified by its
preconditions and postconditions
- **FAIL** If the function does not satisfy the contract specified by its
preconditions and postconditions
- **SKIP** If the function lacks both preconditions and postconditions (in 
which case the precondition is implicitly `T` and the postcondition is
implicitly the cumulative effect of the commands in its body), _or_
if the function body and its enclosing braces are missing (in which case the
contract is simply admitted)

Further documentation about supported parameters is available via

```
java -jar target/prelude.jar --help
```

Because the JVM and cvc5 solver run in the same process, the heap may complete
with the solver for process memory. For jobs that are memory intensive for cvc5
we have found it helpful to fully allocate the heap on startup, and not permit
it to grow larger.

```
java -Xms4g -Xmx4g -jar target/prelude.jar --field-size=2 example/gates-passive.txt
```

## Project structure

This prelude implementation is written in Scala, and adheres to the standard
Maven project structure. Files and locations of note include

- [/example](example) - A collection of example prelude programs
- [/src/main/scala/plaid/prelude/App.scala](/src/main/scala/plaid/prelude/App.scala) - The entry point to the command line application
- [/src/main/antlr4/plaid/prelude/Prelude.g4](src/main/antlr4/plaid/prelude/Prelude.g4) - The ANTLR4 grammar for the prelude language
- [/src/main/scala/plaid/prelude/ast](/src/main/scala/plaid/prelude/ast) - Simplified abstract syntax tree
- [/src/main/scala/plaid/prelude/antlr](/src/main/scala/plaid/prelude/antlr) - Conversion from the ANTLR4 AST to the simplified AST
- [/src/main/scala/plaid/prelude/logic](/src/main/scala/plaid/prelude/logic) - Contract checking
- [/src/main/scala/plaid/prelude/cvc](/src/main/scala/plaid/prelude/cvc) - Bridge to the cvc5 solver

<!--
## MacOS installation with Chris
```
brew install openjdk
```

```
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
```

```
brew install cmake
```
-->
