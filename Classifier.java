import java.io.*;
import java.util.*;

// The Classifier Class will help you figure out if a line of text is spam or not. 
// It contains methods like save, which allow you to save the current
// classifier, and classifier, which labels the text as spam or not.
public class Classifier {
    private ClassifierNode root;

    // this is a constructor
    // Behavior :
    //     - Creates a new Classifier and creates a new classifier using the 
    //       inputed file
    // Parameters : 
    //     - Scanner input : the file with the contents that need to be 
    //                       converted to a classifier
    //     - File format: 
    //                   Feature: here
    //                   Threshold: 0.125
    //                   Ham
    //                   Spam
    // Returns : None
    // Exceptions : 
    //     - throws an IllegalArgumentException if the input is null
    public Classifier(Scanner input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        root = buildTree(input);
    }

    // Behavior :
    //     - Reads a file line by line to build a tree of ClassifierNode objects.
    //     - Recursively builds left and right subtrees for the node.
    // Parameters : 
    //     - Scanner input: A Scanner object containing the tree structure in the 
    //       expected format.
    // Returns : 
    //     - Classifier node: The root ClassifierNode of the constructed tree.
    // Exceptions : 
    //     - throws an IllegalArgumentException if the file is not formated 
    //       the right way (line after Feature does not start with Threshold)
    private ClassifierNode buildTree(Scanner input) {
        if (!input.hasNextLine()) {
            return null;
        }
        String line = input.nextLine().trim();
        if (line.startsWith("Feature: ")) {
            String feature = line.substring("Feature: ".length());
            if (!input.hasNextLine()) { 
                throw new IllegalArgumentException();
            }
            String thresholdLine = input.nextLine().trim();
            if (!thresholdLine.startsWith("Threshold: ")) {
                throw new IllegalArgumentException();
            }
            double threshold = Double.parseDouble(thresholdLine.substring("Threshold: ".length()));
            ClassifierNode node = new ClassifierNode(feature, threshold, buildTree(input), buildTree(input));
            return node;
        } else {
            return new ClassifierNode(line);
        }
    }

    // this is a constructor
    // Behavior :
    //     - creates a new Classifier using the given lists
    // Parameters : 
    //     - List<TextBlock> data : A list of text-based training data samples.
    //     - List<String> labels : A list of corresponding labels for each data sample.
    // Returns : None
    // Exceptions : 
    //     - throws an IllegalArgumentException if
    //          - data is null
    //          - labels is null
    //          - the size of data is not equal to the size of labels
    //          - if data is empty    
    public Classifier(List<TextBlock> data, List<String> labels) {
        if (data == null || labels == null || data.size() != labels.size() || data.isEmpty()) {
            throw new IllegalArgumentException();
        }
        root = new ClassifierNode(labels.get(0), data.get(0));
        for (int i = 1; i < data.size(); i++) {
            root = insertIntoTree(root, data.get(i), labels.get(i));
        }
    }

    // Behavior :
    //     - Inserts a new data point with its label into the decision tree.
    // Parameters : 
    //     - ClassifierNode node : The current node in the decision tree.  
    //     - TextBlock newData : The new data point to be inserted.
    //     - String newLabel : The label associated with the new data. 
    // Returns : 
    //     - ClassifierNode : The updated tree with the new data inserted
    // Exceptions : None 
    private ClassifierNode insertIntoTree(ClassifierNode node, TextBlock newData, String newLabel) {
        ClassifierNode newLeaf = new ClassifierNode(newLabel, newData);
        if (node == null) {
            return newLeaf;
        }
        if (node.label() != null) {
            if (node.label.equals(newLabel)) {
                return node;
            }
            String splitFeature = node.data.findBiggestDifference(newData);
            double threshold = Double.parseDouble("" + midpoint(node.data.get(splitFeature), newData.get(splitFeature)));
            ClassifierNode ogLeaf = new ClassifierNode(node.label, node.data);
            if (newData.get(splitFeature) >= threshold) {
                return new ClassifierNode(splitFeature, threshold, ogLeaf, newLeaf);
            } else {
                return new ClassifierNode(splitFeature, threshold, newLeaf, ogLeaf);
            }
        }
        if (newData.get(node.feature) >= node.threshold) {
            node.right = insertIntoTree(node.right, newData, newLabel);
        } else {
            node.left = insertIntoTree(node.left, newData, newLabel);
        }
        return node;
    }

    // Behavior :
    //      - Classifies a given TextBlock as Spam or Ham.
    // Parameters : 
    //     - TextBlock input : The data to be classified.
    // Returns : 
    //     - String : The label corresponding to the classification.
    // Exceptions : 
    //     - throws an IllegalArgumentException if
    //          - input is null  
    public String classify(TextBlock input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        return classify(root, input);
    }

    // Behavior :
    //     - Recursively classifies a given TextBlock using the decision tree.
    // Parameters : 
    //     - ClassifierNode node : The current node in the tree.
    //     - TextBlock input : The data to classify.
    // Returns : 
    //     - String : The label corresponding to the classification.
    // Exceptions : None  
    private String classify(ClassifierNode node, TextBlock input) {
        if (node == null) {
            return "null";
        }
        if (node.label() != null) {
            return node.label;
        }
        double val = input.get(node.feature);
        if (val < node.threshold) {
            return classify(node.left, input);
        } else {
            return classify(node.right, input);
        }
    }

    // Behavior :
    //     - Saves the decision tree structure to an output stream.
    // Parameters : 
    //     - PrintStream output : The stream to save the tree data.
    // Returns : None
    // Exceptions : 
    //     - throws an IllegalArgumentException if
    //          - output is null  
    public void save(PrintStream output) {
        if (output == null) {
            throw new IllegalArgumentException();
        }
        save(root, output);
    }

    // Behavior :
    //     - Recursively saves the decision tree structure to an output stream.
    // Parameters : 
    //     - ClassifierNode node : The current node in the tree.  
    //     - PrintStream output : The stream to save the tree data.
    // Returns : None
    // Exceptions : None
    private void save(ClassifierNode node, PrintStream output) {
        if(node != null) {
            if (node.label() != null) {
                output.println(node.label);
            } else {
                output.println("Feature: " + node.feature);
                output.println("Threshold: " + node.threshold);
                save(node.left, output);
                save(node.right, output);
            }
        }
    }

    // the ClassifierNode class is a node class that represents the decision nodes and the label nodes
    private static class ClassifierNode {
        public final String feature;
        public final double threshold;
        public final String label;
        public ClassifierNode left;
        public ClassifierNode right;
        public final TextBlock data;

        // this is a constructor
        // Behavior:
        //     - Initializes a node with a feature and threshold.
        // Parameters:
        //     - String feature: The feature used for decision-making.
        //     - double threshold: The threshold value for splitting.
        // Returns: None
        // Exceptions: None
        public ClassifierNode(String feature, double threshold) {
            this.feature = feature;
            this.threshold = threshold;
            this.label = null;
            this.left = null;
            this.right = null;
            this.data = null;
        }

        // this is a constructor
        // Behavior:
        //     - Initializes a node with a feature and threshold, left and right node
        // Parameters:
        //     - String feature: The feature used for decision-making.
        //     - double threshold: The threshold value for splitting.
        //     - ClassifierNode left: the reference to the left node
        //     - ClassifierNode right: the reference to the right node
        // Returns: None
        // Exceptions: None
        public ClassifierNode(String feature, double threshold, ClassifierNode left, ClassifierNode right) {
            this(feature, threshold);
            this.left = left;
            this.right = right;
        }

        // Behavior:
        //     - Initializes a node with a label and data
        // Parameters:
        //     - String feature: The feature used for decision-making.
        //     - TextBlock data: The data associated with this leaf node.
        // Returns: None
        // Exceptions: None
        public ClassifierNode(String label, TextBlock data) {
            this.label = label;
            this.threshold = 0;
            this.feature = null;
            this.data = data;
        }

        // Behavior:
        //     - Initializes a node with a label
        // Parameters:
        //     - String label: The label associated with this leaf node.
        // Returns: None
        // Exceptions: None
        public ClassifierNode(String label) {
            this.label = label;
            this.threshold = 0;
            this.feature = null;
            this.data = null;
        }

        // Behavior:
        //     - returns the label
        // Parameters: None
        // Returns: 
        //     - String: the label
        // Exceptions: None
        public String label() {
            return label;
        }

        // Behavior:
        //     - sets the the left node
        // Parameters: 
        //     - ClassifierNode left: the node that this node needs to reference
        // Returns: None
        // Exceptions: None
        public void setLeft(ClassifierNode left) {
            this.left = left;
        }

        // Behavior:
        //     - sets the the right node
        // Parameters: 
        //     - ClassifierNode right: the node that this node needs to reference
        // Returns: None
        // Exceptions: None
        public void setRight(ClassifierNode right) {
            this.right = right;
        }

        // Behavior:
        //     - returns the left node
        // Parameters: None
        // Returns: 
        //     - ClassifierNode: the node
        // Exceptions: None
        public ClassifierNode getLeft() {
            return left;
        }

        // Behavior:
        //     - returns the right node
        // Parameters: None
        // Returns: 
        //     - ClassifierNode: the node
        // Exceptions: None
        public ClassifierNode getRight() {
            return right;
        }
    }

    ////////////////////////////////////////////////////////////////////
    // PROVIDED METHODS - **DO NOT MODIFY ANYTHING BELOW THIS LINE!** //
    ////////////////////////////////////////////////////////////////////

    // Helper method to calcualte the midpoint of two provided doubles.
    private static double midpoint(double one, double two) {
        return Math.min(one, two) + (Math.abs(one - two) / 2.0);
    }    

    // Behavior: Calculates the accuracy of this model on provided Lists of 
    //           testing 'data' and corresponding 'labels'. The label for a 
    //           datapoint at an index within 'data' should be found at the 
    //           same index within 'labels'.
    // Exceptions: IllegalArgumentException if the number of datapoints doesn't match the number 
    //             of provided labels
    // Returns: a map storing the classification accuracy for each of the encountered labels when
    //          classifying
    // Parameters: data - the list of TextBlock objects to classify. Should be non-null.
    //             labels - the list of expected labels for each TextBlock object. 
    //             Should be non-null.
    public Map<String, Double> calculateAccuracy(List<TextBlock> data, List<String> labels) {
        // Check to make sure the lists have the same size (each datapoint has an expected label)
        if (data.size() != labels.size()) {
            throw new IllegalArgumentException(
                    String.format("Length of provided data [%d] doesn't match provided labels [%d]",
                                  data.size(), labels.size()));
        }
        
        // Create our total and correct maps for average calculation
        Map<String, Integer> labelToTotal = new HashMap<>();
        Map<String, Double> labelToCorrect = new HashMap<>();
        labelToTotal.put("Overall", 0);
        labelToCorrect.put("Overall", 0.0);
        
        for (int i = 0; i < data.size(); i++) {
            String result = classify(data.get(i));
            String label = labels.get(i);

            // Increment totals depending on resultant label
            labelToTotal.put(label, labelToTotal.getOrDefault(label, 0) + 1);
            labelToTotal.put("Overall", labelToTotal.get("Overall") + 1);
            if (result.equals(label)) {
                labelToCorrect.put(result, labelToCorrect.getOrDefault(result, 0.0) + 1);
                labelToCorrect.put("Overall", labelToCorrect.get("Overall") + 1);
            }
        }

        // Turn totals into accuracy percentage
        for (String label : labelToCorrect.keySet()) {
            labelToCorrect.put(label, labelToCorrect.get(label) / labelToTotal.get(label));
        }
        return labelToCorrect;
    }
}
