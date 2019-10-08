Web Search Engine and Information Retrieval
Assignment 2 Group 1
Created by: Zhirou Zhou, s3674467 & Pengqian Li, s3676048

Compile two java source files using:
    javac *.java

index Usage:

    java index [-s <stoplist>] [-p] <sourcefile>.
    -s <stoplist> is an optional argument which indicate a stoplist.
    -p is an optional argument that determines whether print the filtered term to screen
    <sourcefile> is the path of the source file which should be always the last argument.

    e.g. java index -s stoplist latimes-100

search Usage:
    java search -BM25 -q <query-label> -n <num-results> -l <lexicon> -i <invlists> -m map 
    [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]

    e.g. java search -BM25 -q 401 -n 10 -l lexicon -i invlists -m map volcano america active

advanced Usage:
    java advanced -q <query-label> -n <num-results> -l <lexicon> -i <invlists> -m map 
    -s <stoplist> <queryterm-1> [<queryterm-2> ... <queryterm-N>]

    e.g. java advanced -q 401 -n 5 -l lexicon -i invlists -m map -s stoplist volcano america active

The time consumption of indexing could be very long due to that the source file is needed to be split into individual files which contains one document each. This is for summarisation function to retrieve document directly from the target file instead of reading the whole source file again.