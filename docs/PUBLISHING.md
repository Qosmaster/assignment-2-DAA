# Publish the project to GitHub

The ZIP contains a real local Git repository, including the hidden `.git` folder.
It has preparation-stage commits made while this package was built, under the
neutral author `Assignment Project Builder`. These are not historical commits
made by the student. Review the code, make your own genuine changes as needed,
and preserve an honest account of the work.

A remote GitHub repository has not been created or uploaded by this package.
The assignment requires a repository URL, not just a ZIP file or a Word report.

## 1. Extract and verify

Extract the ZIP. Open a terminal in its `assignment-2` folder, not in its parent.
Check that the repository and files are present:

```sh
git status
git log --oneline
```

Run `sh run.sh` on macOS/Linux or `run.bat` on Windows. A JDK is required.
Java tests and benchmarks do not require Python.

## 2. Set the author for your future commits

Use your own Git author details; the existing preparation commits should not be
rewritten or backdated to impersonate earlier student development.

```sh
git config user.name "Your Name"
git config user.email "Your Git email address"
```

After actual edits or new experiments, commit those genuine changes with a
meaningful message describing what changed. There is no reason to manufacture
extra commits just to increase the count.

## 3. Publish to an empty repository

Create an empty repository called `assignment-2` in your own GitHub account.
Choose visibility according to the course rules, and give the instructor access
if it is private. Do not initialize the remote with another README or license,
because this project already has an existing history.

Replace `YOUR_USERNAME` in the following remote address with your actual account:

```sh
git remote add origin https://github.com/YOUR_USERNAME/assignment-2.git
git push -u origin main
```

These commands assume no `origin` remote exists yet. Inspect `git remote -v`
first when working in an already-published copy; do not overwrite another remote
without checking it. Complete GitHub authentication through your normal client.
Never paste an account password or access token into the project files.

## 4. Final submission checks

Open the hosted repository and verify that it contains `README.md`, `src/`,
`results/tables/`, `results/plots/`, and `report/Assignment_2_Report.docx`.
The README images should render. Do not upload only the ZIP as one repository file.
Submit the repository's URL and the individual report wherever the course requires.

Ask the instructor to confirm the valid-batch resolution for Workload 3 before
claiming literal compliance with its impossible small-n removal instruction.
The details are in README Section 4.3. No score or approval is guaranteed here.
