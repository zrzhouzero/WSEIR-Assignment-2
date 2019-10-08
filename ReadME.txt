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

    eg. java index -s stoplist latimes-100

search Usage:
    java search -BM25 -q <query-label> -n <num-results> -l <lexicon> -i <invlists> -m map 
    [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]

    eg. java search 

advanced Usage:
    java advanced -q <query-label> -n <num-results> -l <lexicon> -i <invlists> -m map 
    -s <stoplist> <queryterm-1> [<queryterm-2> ... <queryterm-N>]