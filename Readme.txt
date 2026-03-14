Information Retrieval - Programming Assignment 1: Text Parser

DESCRIPTION
This project implements a Text Parser that reads TREC-formatted documents, tokenizes the text, removes stopwords, and applies the Porter Stemmer algorithm. It outputs a sorted Term Dictionary and a Document Dictionary, mapping unique words and document names to unique IDs.

PROJECT STRUCTURE
IR_Text_Parser/
├── src/
│   ├── TextParser.java
│   ├── Porter.java
│   └── parser_output.txt       (Generated automatically upon execution)
├── data/
│   ├── ft911/                  (Directory containing all TREC text files)
│   └── stopwordlist.txt        (Text file containing stopwords, one per line)
└── Readme.txt

HOW TO COMPILE
1. Open your terminal or command prompt.
2. Navigate directly into the "src" directory of the project:
   cd path/to/IR_Text_Parser/src
3. Compile the Java files by running the following command:
   javac TextParser.java Porter.java

HOW TO RUN (USING STRICT RELATIVE PATHS)
1. Ensure your terminal is still located inside the "src" directory.
2. Run the program by passing the strict relative paths to the data folder. The command steps out of "src" (using ../) and goes into "data":
   java TextParser ../data/ft911/ ../data/stopwordlist.txt

OUTPUT
After successful execution, the program will generate a file named "parser_output.txt" directly inside the "src/" directory. 
- Words are sorted alphabetically prior to ID assignment.
- Document IDs are strictly parsed to match the numeric value in the FT911-X filename.