RM Detector Feature Extraction
=====

This code takes raw input and generates aggregrated, tagged data for input into a feature selection algorithm for the generation of detectors of student affect and behavior.

The main code is [RMGenerateFeatures.java](src/RMGenerateFeatures.java).

The code requires a single command-line argument which is the folder containing the student logs.  The directory must contain:

- `baker_ses_classes.csv` and `vw_classes.csv`: The raw log files.
- `syncedDataAll.txt`: The observation logs.
- `PofJ.txt`, `PofG.txt`, and `PofS.txt`: The parameters, determined by linear regression, for the calculation of contextual BKT parameters.
- [`generatecontextual.py`](src/generatecontextual.py) and [`contextualfeatures.py`](src/contextualfeatures.py): Copy these scripts to the log file directory.

The result, contained in a folder called `clips`, will be the data, aggregated into clips, for all observations as well as divided by label.

__Note__: _Intermediate files are retained, and can be quite large.  Upon completion of the process, the directory is likely to contain around 10 GB of files._

__Note__: _The step of correcting the observation sync, performed by the method `RMGenerateFeatures.setObs()`, takes quite a long time.  It's stongly recommended that this command by executed once, and the resulting file, `syncedDataAll_new.txt` be renamed to `syncedDataAll.txt`, with the call to `setObs()` commented out in the code._
