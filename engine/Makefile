# Builds build/fish (the C++ engine) and build/host (its Java half).
# Needs JAVA_HOME (JDK 21) and build/engine-classpath.txt (./gradlew engineClasspath); tools/build does both.

JAVA_HOME ?= $(HOME)/.cache/caterpillow/jdk21
CXX ?= g++
# -ffp-contract=off keeps float math identical to Java (no fused multiply-add).
CXXFLAGS ?= -O2 -g -std=c++20 -Wall -Wextra -Wno-unused-parameter -ffp-contract=off -fno-fast-math
JNI_INC := -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
JVM_LIB := $(JAVA_HOME)/lib/server

SRC := $(wildcard src/*.cpp src/game/*.cpp src/host/*.cpp)
OBJ := $(patsubst src/%.cpp,build/obj/%.o,$(SRC))
DEP := $(OBJ:.o=.d)
JAVA_SRC := $(wildcard java/fish/*.java)
ENGINE_CP = $(shell cat build/engine-classpath.txt)

all: build/fish build/host.stamp build/classpath.txt build/ref.stamp

build/fish: $(OBJ)
	$(CXX) $(CXXFLAGS) -o $@ $(OBJ) -L$(JVM_LIB) -ljvm -Wl,-rpath,$(JVM_LIB)

build/obj/%.o: src/%.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(JNI_INC) -MMD -MP -c $< -o $@

build/host.stamp: $(JAVA_SRC) build/engine-classpath.txt
	@rm -rf build/host && mkdir -p build/host
	$(JAVA_HOME)/bin/javac -nowarn -encoding UTF-8 -source 21 -target 21 \
	  --add-exports java.base/jdk.internal.vm=ALL-UNNAMED \
	  -cp "$(ENGINE_CP)" -d build/host $(JAVA_SRC)
	@touch $@

build/classpath.txt: build/engine-classpath.txt
	echo "$(abspath build/host):$(ENGINE_CP)" > $@

build/engine-classpath.txt:
	@echo "missing $@: run ./gradlew engineClasspath (tools/build does)" && exit 1

clean:
	rm -rf build/obj build/fish build/host build/host.stamp build/classpath.txt

.PHONY: all clean
-include $(DEP)

# The official engine wrapped to print fish's per-round state hash (tools/verify).
REF_SRC := $(wildcard ref/fishref/*.java)
ref: build/ref.stamp
build/ref.stamp: $(REF_SRC) build/engine-classpath.txt
	@rm -rf build/ref && mkdir -p build/ref
	$(JAVA_HOME)/bin/javac -nowarn -encoding UTF-8 -cp "$(ENGINE_CP)" -d build/ref $(REF_SRC)
	@touch $@
.PHONY: ref
