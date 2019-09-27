import java.io.*;
import java.util.*;

public class BigramBayespam {
    // This defines the two types of messages we have.
    static enum MessageType { NORMAL, SPAM }

    /// This defines the epsilon.
    public final static int EPSILON = 1;

    // This a class with two counters (for regular and for spam)
    static class MultipleCounter {
        int counterSpam    = 0;
        int counterRegular = 0;

        // Increase one of the counters by one
        public void incrementCounter(MessageType type) {
            if (type == MessageType.NORMAL) {
                ++counterRegular;
            } else {
                ++counterSpam;
            }
        }
    }

    // Listings of the two subdirectories (regular/ and spam/)
    private static File[] listingRegular = new File[0];
    private static File[] listingSpam = new File[0];

    // A hash table for the vocabulary 
    // (word searching is very fast in a hash table)
    private static Hashtable<String, MultipleCounter> vocab 
            = new Hashtable<String, MultipleCounter>();
    
    // Add a bigram to the vocabulary
    private static void addBigram(String bigram, MessageType type) {
        MultipleCounter counter = new MultipleCounter();
        // If word exists already in the vocabulary..
        if (vocab.containsKey(bigram)) {
            // Get the counter from the hashtable
            counter = vocab.get(bigram);
        }

        // Increase the counter appropriately
        counter.incrementCounter(type);

        // Put the word with its counter into the hashtable
        vocab.put(bigram, counter);
    }


    // List the regular and spam messages
    private static void listDirs(File dirLocation) {
        // List all files in the directory passed
        File[] dirListing = dirLocation.listFiles();
	    String folderName; 
	    Boolean spamFound = false, regularFound = false;

        // Check that there are exactly 2 subdirectories
        if (dirListing.length != 2) {
            System.out.println("- Error: the directory should contain exactly"
                               + " 2 subdirectories (named spam and regular)."
                               + "\n");
            Runtime.getRuntime().exit(0);
        }
	
        // Loop through all subdirectories
        for (File file : dirListing) {
            folderName = file.toString();
            // If the folder_name ends in the word spam,
            // store it as the spam folder
            if (folderName.length() > 3 
                    && folderName.substring(folderName.length() - 4).equals("spam")) {
                listingSpam = file.listFiles();
                spamFound = true;
                // If the folder_name ends in the word regular, 
                // store it as the regular folder
            } else if (folderName.length() > 6 
                    && folderName.substring(folderName.length() - 7).equals("regular")) {
                listingRegular = file.listFiles();
                regularFound = true;
            }
        }
	
        if (!spamFound) {
            System.out.println("- Error: directory with spam messages not found."
                               + " Make sure your input directory"
                               + " contains a folder named spam.\n");
            Runtime.getRuntime().exit(0);
        }

        if (!regularFound) {
            System.out.println("- Error: directory with regular messages not found."
                               + " Make sure your input directory"
                               + " contains a folder named regular\n");
            Runtime.getRuntime().exit(0);
        }
    }
    
    // Print the current content of the vocabulary
    private static void printVocab() {
        MultipleCounter counter = new MultipleCounter();

        for (Enumeration<String> enumeration = vocab.keys();
                enumeration.hasMoreElements() ;) {   
            String bigram;
            
            bigram = enumeration.nextElement();
            counter  = vocab.get(bigram);
            
            System.out.println( bigram
                               + " | in regular: " 
                               + counter.counterRegular 
                               + " in spam: " 
                               + counter.counterSpam);
        }
    }
    
    /// function for checking numerals
    public static boolean isNumeric(final CharSequence charSequence) {
        final int size = charSequence.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(charSequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    // Read the words from messages and add them to your vocabulary.
    // The boolean type determines whether the messages are regular or not  
    private static void readMessages(MessageType type) throws IOException {
        File[] messages = new File[0];

        messages = (type == MessageType.NORMAL) ? listingRegular : listingSpam;

        for (int i = 0; i < messages.length; ++i) {
            FileInputStream fileInputStream = new FileInputStream(messages[i]);
            BufferedReader in = 
                    new BufferedReader(new InputStreamReader(fileInputStream));
            String line, word;
            // Read a line
            while ((line = in.readLine()) != null) {                

                /// Remove punctuation
                line = line.replaceAll("\\p{Punct}","");

                /// Convert to lower case
                line = line.toLowerCase();                

                // Parse it into words
                StringTokenizer st = new StringTokenizer(line);                

                // While there are stille words left..
                while (st.hasMoreTokens()) {
                    final int minSize = 4;
                    String first = st.nextToken();
                    if (first.length() < minSize) {
                        continue;
                    }
                    while (st.hasMoreTokens()) {
                        String second = st.nextToken();
                        if (second.length() < minSize) {
                            continue;
                        }
                        String token = first + " " + second;
                        addBigram(token, type);
                        break;
                    }

                    /// Do not accept words that have less than 4 letters
                    /// Only accept words that have less than 4 characters
                    /// and that are not numeric.
                    //if (token.length() >= 4 && !isNumeric(token)) {

                        // Add them to the vocabulary
                        //addBigram(token, type);                                          
                    //}
                }
            }
            in.close();
        }
    }
   
    public static void main(String[] args)  throws IOException {
        // Location of the directory (the path) 
        // taken from the cmd line (first arg)
        File dirLocation = new File(args[0]);
        
        // Check if the cmd line arg is a directory
        if (!dirLocation.isDirectory()) {
            System.out.println("- Error: cmd line arg not a directory.\n");
            Runtime.getRuntime().exit(0);
        }

        // Initialize the regular and spam lists
        listDirs(dirLocation);

        // Read the e-mail messages
        readMessages(MessageType.NORMAL);
        readMessages(MessageType.SPAM);

        // Print out the hash table
        printVocab();
        System.out.println(listingRegular.length);
        System.out.println(listingSpam.length);

        /// Calculat a priori class probabilities.
        int totalMessages = listingRegular.length + listingSpam.length;
        double regularPrioriProbability = listingRegular.length / totalMessages;
        double spamPrioriProbability = listingSpam.length / totalMessages; 

        /// Count the number of words in the vocab of the regular and spam mails.
        Set<Map.Entry<String, MultipleCounter>> entrySet = vocab.entrySet();
        int sizeRegularVocab = 0;
        int sizeSpamVocab = 0;
        for (Map.Entry<String, MultipleCounter> entry : entrySet) {
            sizeRegularVocab += entry.getValue().counterRegular;
            sizeSpamVocab += entry.getValue().counterSpam;
        }

        Hashtable<String, CategoricalProbabilities> vocabProbabilities
                = new Hashtable<String, CategoricalProbabilities>();

        // Calculate class conditionals.
        for (Map.Entry<String, MultipleCounter> entry : entrySet) {
            double regularProbability = entry.getValue().counterRegular
                                        / sizeRegularVocab;
            double spamProbability = entry.getValue().counterSpam 
                                     / sizeSpamVocab;

            if (regularProbability == 0) {
                regularProbability = 
                        EPSILON / (sizeRegularVocab + sizeSpamVocab);
            }

            if (spamProbability == 0) {
                spamProbability =
                        EPSILON / (sizeRegularVocab + sizeSpamVocab);
            }

            CategoricalProbabilities probabilities = 
                    new CategoricalProbabilities(regularProbability, 
                                                 spamProbability);
            vocabProbabilities.put(entry.getKey(), probabilities);
        }

        // Now all students must continue from here:
        //
        // 1) A priori class probabilities must be computed from the number of regular
        //    and spam messages
        // 2) The vocabulary must be clean: punctuation and digits must be removed,
        //    case insensitive
        // 3) Conditional probabilities must be computed for every word
        // 4) A priori probabilities must be computed for every word
        // 5) Zero probabilities must be replaced by a small estimated value
        // 6) Bayes rule must be applied on new messages, followed by argmax classification
        // 7) Errors must be computed on the test set (FAR = false accept rate (misses),
        //    FRR = false reject rate (false alarms))
        // 8) Improve the code and the performance (speed, accuracy)
        //
        // Use the same steps to create a class BigramBayespam 
        // which implements a classifier using a vocabulary consisting of bigrams
    }
}
