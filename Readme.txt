Information Retrieval - Programming Assignment 2: Search Engine Index Construction

DESCRIPTION
This project builds upon the Phase 1 Text Parser to construct a functioning Indexer for an Information Retrieval Engine. It processes TREC-formatted text files, standardizes tokens using a Porter Stemmer, and dynamically builds two structures:
1. Forward Index: Maps each document ID to a list of word IDs and their frequencies.
2. Inverted Index: Maps each word ID to the documents containing it and their frequencies.

The program outputs these indices as text files and provides an interactive command-line interface to search for specific terms and retrieve their posting lists.

PROJECT STRUCTURE
IR_Text_Parser/
├── src/
│   ├── TextParser.java
│   ├── Porter.java
│   ├── forward_index.txt       (Generated automatically)
│   └── inverted_index.txt      (Generated automatically)
├── data/
│   ├── ft911/                  (Directory containing all TREC text files)
│   ├── testdata.txt            (For specific test files)
│   └── stopwordlist.txt        (Text file containing stopwords)
├── Readme.txt
└── project-report.pdf

HOW TO COMPILE
1. Open your terminal or command prompt.
2. Navigate directly into the "src" directory of the project:
   cd path/to/IR_Text_Parser/src
3. Compile the Java files by running the following command:
   javac TextParser.java Porter.java

HOW TO RUN
1. Ensure your terminal is located inside the "src" directory.
2. Run the program using relative paths pointing to the data folder:
   java TextParser ../data/ft911/ ../data/stopwordlist.txt

*(Note: To test a specific file instead of the whole directory, replace `../data/ft911/` with the path to the text file, e.g., `../data/testdata.txt`)*

INTERACTIVE SEARCHING
Once the indexing process is complete, an interactive console will appear.
- Type any English word (e.g., "computer", "assign").
- The system will stem it and return its exact posting list in the required format: `term: docID:freq ; docID:freq`
- Type `EXIT` to close the application.