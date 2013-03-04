Org -> Docbook -> Habr exporter
===============================

Converts Docbook output of Emacs Org Mode exporter into [Habrahabr](http://habrahabr.ru) markup (subset of HTML). 
As a bonus provides an ability to select source type for code blocks, caching the selection so it won't be
necessary to select correct language again on the next run.

Usage
-----

Either
```
  lein run <source docbook xml file> <target text file>
```
or just `lein run` and then select a file using convenient dialog.
