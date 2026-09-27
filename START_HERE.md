# Start here

## Open and test the project

Extract the ZIP first. On Windows, open the `assignment-2` folder and double-click
`check.bat`. A JDK must be installed. The project has been tested with JDK 21.
The script compiles all Java files, shows the small demo, and runs the tests.
It leaves the existing benchmark results unchanged.

On macOS/Linux, open a terminal in the same folder and run `sh check.sh`.

You can also use a terminal from the project folder:

```
javac -Xlint:all -d out src/*.java
java -cp out Demo
java -cp out Tests
```

The test output should end with:

```
PASS: 188698 checks; all test groups passed.
```

## What to read

Start with `src/Demo.java`, then `DynamicArray.java`, `LinkedList.java`, and
`MinHeap.java`. `docs/DEFENSE_GUIDE.md` explains the methods and the basic Java
syntax. `Benchmark.java` handles the required experiments; `Tests.java` checks
answers. `IntList.java` is a small common interface, and `Metrics.java` stores
counts. The Python scripts are optional tools for graphs and Word files, not
part of the Java data-structure implementations.

The report is `report/Assignment_2_Report.docx`. Its text and numbers also appear
in `README.md`. Fill in the student name and group in the Word report. Review the
work before submitting it, and follow your course rules about assistance.

## Screenshots

The supplied brief does not separately ask for screenshots. It requires tables
and graphs, which are already in the report, README, and `results` folder.
A screenshot of your own successful `check.bat` run is optional extra evidence.
Do not use a screenshot as a replacement for the source files or result files.

## New measurements

`run.bat` on Windows, or `sh run.sh` on macOS/Linux, runs the full benchmark.
It overwrites the measurement CSV files and environment information.
Use a copy of the project to keep the supplied results unchanged.

After new measurements, regenerate every derived file together:

```
python -m pip install matplotlib python-docx
python scripts/verify_results.py
python scripts/make_plots.py
python scripts/build_report.py
```

The source uses no external Java libraries. Python is only needed to regenerate
plots and the Word report. A report rebuild restores the blank name/group fields,
so add your details after the final rebuild. New times will differ by computer.

## Before submitting

The brief asks for a GitHub repository link and an individual report.
Publishing the repository is still needed. Follow `docs/PUBLISHING.md` and keep
the existing honest development history. Do not upload only the ZIP as one file.

Ask the instructor to approve the Workload 3 rule in report Section 4.2: the code
restores a small structure outside timing when 1,000 removals cannot fit in it.
All other workload sizes and operation counts stay as specified in the brief.
