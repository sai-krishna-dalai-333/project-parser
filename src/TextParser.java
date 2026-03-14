import java.io.*;
import java.util.*;

public class TextParser {

    private Set<String> terms_set = new TreeSet<>();
    private Map<String, Integer> trec_doc_map = new TreeMap<>();
    private Set<String> stop_words_set = new HashSet<>();
    private Porter porter_obj = new Porter();

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
        
        output_results();
    }

    private void trec_file_read(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        boolean inside_text = false;
        StringBuilder sb = new StringBuilder();
        String curr_doc_name = "";

        while ((line = br.readLine()) != null) {
            if (line.contains("<DOCNO>")) {
                curr_doc_name = line.replace("<DOCNO>", "").replace("</DOCNO>", "").trim();
                try {
                    String[] split_id = curr_doc_name.split("-");
                    if (split_id.length > 1) {
                        int numeric_id = Integer.parseInt(split_id[1].trim());
                        trec_doc_map.put(curr_doc_name, numeric_id);
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("exceoption @ doc " + curr_doc_name);
                }

            } else if (line.contains("<TEXT>")) {
                inside_text = true;
            } else if (line.contains("</TEXT>")) {
                inside_text = false;
                extract_words(sb.toString());
                sb.setLength(0); 
            } else if (inside_text) {
                sb.append(line).append(" ");
            }
        }
        br.close();
    }

    private void extract_words(String raw_text) {
        String[] tokens = raw_text.toLowerCase().split("[^a-z0-9]+");
        
        for (String t : tokens) {
            if (t.isEmpty() || t.matches(".*\\d.*") || stop_words_set.contains(t)) {
                continue;
            }
            
            String stemmed_val = porter_obj.stripAffixes(t);
            
            if (!stemmed_val.isEmpty()) {
                terms_set.add(stemmed_val);
            }
        }
    }

    private void output_results() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("parser_output.txt"));
        
        int id_counter = 1;
        for (String term : terms_set) {
            bw.write(term + "\t" + id_counter + "\n");
            id_counter++;
        }
        
        for (Map.Entry<String, Integer> entry : trec_doc_map.entrySet()) {
            bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        
        bw.close();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.exit(1);
        }

        try {
            TextParser tp = new TextParser();
            tp.stop_words_file_load(args[1]);
            tp.directory_parse(args[0]);
            System.out.println(" Output saved to src/parser_output.txt");
        } catch (IOException e) {
            System.err.println("File reading error: " + e.getMessage());
        }
    }
}