import java.io.*;
import java.util.*;

public class TextParser {

    private Map<String, Integer> trec_doc_map = new TreeMap<>();
    private Set<String> stop_words_set = new HashSet<>();
    private Map<String, Integer> term_id_map = new TreeMap<>();
    private Porter porter_obj = new Porter();

    // Phase 2: Index Data Structures
    // Forward Index: docID -> (Word String -> Frequency)
    private Map<Integer, Map<String, Integer>> forward_index_temp = new TreeMap<>();
    // Inverted Index: Word String -> (docID -> Frequency)
    private Map<String, Map<Integer, Integer>> inverted_index_temp = new TreeMap<>();

    public void stop_words_file_load(String stopFilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(stopFilePath));
        String currLine;
        
        while ((currLine = br.readLine()) != null) {
            String lower_case_word = currLine.trim().toLowerCase();
            if (!lower_case_word.isEmpty()) {
                stop_words_set.add(lower_case_word);
            }
        }
        br.close();
    }

    public void directory_parse(String targetPath) throws IOException {
        File path_obj = new File(targetPath);
        
        if (path_obj.isDirectory()) {
            File[] all_files = path_obj.listFiles();
            if (all_files != null) {
                Arrays.sort(all_files); 
                for (File f : all_files) {
                    if (f.isFile()) {
                        trec_file_read(f);
                    }
                }
            }
        } else if (path_obj.isFile()) {
            trec_file_read(path_obj);
        }
        
        export_indices();
    }

    private void trec_file_read(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        boolean inside_text = false;
        StringBuilder sb = new StringBuilder();
        String curr_doc_name = "";
        int current_doc_id = -1;

        while ((line = br.readLine()) != null) {
            if (line.contains("<DOCNO>")) {
                curr_doc_name = line.replace("<DOCNO>", "").replace("</DOCNO>", "").trim();
                try {
                    String[] split_id = curr_doc_name.split("-");
                    if (split_id.length > 1) {
                        current_doc_id = Integer.parseInt(split_id[1].trim());
                        trec_doc_map.put(curr_doc_name, current_doc_id);
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Exception parsing document ID at: " + curr_doc_name);
                }

            } else if (line.contains("<TEXT>")) {
                inside_text = true;
            } else if (line.contains("</TEXT>")) {
                inside_text = false;
                if (current_doc_id != -1) {
                    extract_words(sb.toString(), current_doc_id);
                }
                sb.setLength(0); 
            } else if (inside_text) {
                sb.append(line).append(" ");
            }
        }
        br.close();
    }

    private void extract_words(String raw_text, int doc_id) {
        String[] tokens = raw_text.toLowerCase().split("[^a-z0-9]+");
        
        for (String t : tokens) {
            if (t.isEmpty() || t.matches(".*\\d.*") || stop_words_set.contains(t)) {
                continue;
            }
            
            String stemmed_val = porter_obj.stripAffixes(t);
            
            if (!stemmed_val.isEmpty()) {
                // Populate Forward Index
                forward_index_temp.putIfAbsent(doc_id, new TreeMap<>());
                Map<String, Integer> doc_words = forward_index_temp.get(doc_id);
                doc_words.put(stemmed_val, doc_words.getOrDefault(stemmed_val, 0) + 1);

                // Populate Inverted Index
                inverted_index_temp.putIfAbsent(stemmed_val, new TreeMap<>());
                Map<Integer, Integer> word_docs = inverted_index_temp.get(stemmed_val);
                word_docs.put(doc_id, word_docs.getOrDefault(doc_id, 0) + 1);
            }
        }
    }

    private void export_indices() throws IOException {
        // 1. Assign unique IDs to words alphabetically
        int word_id_counter = 1;
        for (String word : inverted_index_temp.keySet()) {
            term_id_map.put(word, word_id_counter);
            word_id_counter++;
        }

        // 2. Export Word Dictionary
        BufferedWriter word_writer = new BufferedWriter(new FileWriter("word_dict.txt"));
        for (Map.Entry<String, Integer> entry : term_id_map.entrySet()) {
            word_writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        word_writer.close();

        // 3. Export Document Dictionary
        BufferedWriter doc_writer = new BufferedWriter(new FileWriter("doc_dict.txt"));
        for (Map.Entry<String, Integer> entry : trec_doc_map.entrySet()) {
            doc_writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        doc_writer.close();

        // 4. Export Forward Index
        BufferedWriter fwd_writer = new BufferedWriter(new FileWriter("forward_index.txt"));
        for (Map.Entry<Integer, Map<String, Integer>> entry : forward_index_temp.entrySet()) {
            int d_id = entry.getKey();
            
            // Map string words to their numeric IDs so the text file contains only integers
            Map<Integer, Integer> numeric_word_freqs = new TreeMap<>();
            for (Map.Entry<String, Integer> str_entry : entry.getValue().entrySet()) {
                numeric_word_freqs.put(term_id_map.get(str_entry.getKey()), str_entry.getValue());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(d_id).append(":");
            for (Map.Entry<Integer, Integer> num_entry : numeric_word_freqs.entrySet()) {
                sb.append(" ").append(num_entry.getKey()).append(": ").append(num_entry.getValue()).append(";");
            }
            fwd_writer.write(sb.toString() + "\n");
        }
        fwd_writer.close();

        // 5. Export Inverted Index
        BufferedWriter inv_writer = new BufferedWriter(new FileWriter("inverted_index.txt"));
        for (Map.Entry<String, Map<Integer, Integer>> entry : inverted_index_temp.entrySet()) {
            int w_id = term_id_map.get(entry.getKey());
            StringBuilder sb = new StringBuilder();
            sb.append(w_id).append(":");
            for (Map.Entry<Integer, Integer> doc_entry : entry.getValue().entrySet()) {
                sb.append(" ").append(doc_entry.getKey()).append(": ").append(doc_entry.getValue()).append(";");
            }
            inv_writer.write(sb.toString() + "\n");
        }
        inv_writer.close();
    }

    public void test_search_interface() {
        Scanner cli_scanner = new Scanner(System.in);
        System.out.println("\n======================================");
        System.out.println("  Search Engine Index Tester Ready");
        System.out.println("======================================");
        
        while (true) {
            System.out.print("\nEnter a term to search (or type 'EXIT' to quit): ");
            String input_term = cli_scanner.nextLine().trim();
            
            if (input_term.equalsIgnoreCase("EXIT")) {
                break;
            }
            if (input_term.isEmpty()) {
                continue;
            }

            String lower_term = input_term.toLowerCase();
            if (stop_words_set.contains(lower_term)) {
                System.out.println("The term '" + input_term + "' is a stopword and is not indexed.");
                continue;
            }

            String stemmed_term = porter_obj.stripAffixes(lower_term);
            if (!inverted_index_temp.containsKey(stemmed_term)) {
                System.out.println("The term '" + input_term + "' does not exist in the index.");
            } else {
                Map<Integer, Integer> posting_list = inverted_index_temp.get(stemmed_term);
                StringBuilder output_str = new StringBuilder();
                output_str.append(input_term).append(": ");
                
                boolean is_first = true;
                for (Map.Entry<Integer, Integer> doc_freq : posting_list.entrySet()) {
                    if (!is_first) {
                        output_str.append("; ");
                    }
                    output_str.append(doc_freq.getKey()).append(":").append(doc_freq.getValue());
                    is_first = false;
                }
                System.out.println(output_str.toString());
            }
        }
        cli_scanner.close();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java TextParser <corpus_path> <stopwords_path>");
            System.exit(1);
        }

        try {
            TextParser tp = new TextParser();
            
            long start_time = System.currentTimeMillis();
            System.out.println("Loading Stopwords...");
            tp.stop_words_file_load(args[1]);
            
            System.out.println("Parsing directory and building indices...");
            tp.directory_parse(args[0]);
            
            long end_time = System.currentTimeMillis();
            
            System.out.println("------------------------------------------------");
            System.out.println("Indexing Complete! Time taken: " + (end_time - start_time) + " ms");
            System.out.println("Exported files to src/:");
            System.out.println(" - word_dict.txt");
            System.out.println(" - doc_dict.txt");
            System.out.println(" - forward_index.txt");
            System.out.println(" - inverted_index.txt");
            System.out.println("------------------------------------------------");
            
            // Launch the interactive testing tool
            tp.test_search_interface();

        } catch (IOException e) {
            System.err.println("File processing error: " + e.getMessage());
        }
    }
}