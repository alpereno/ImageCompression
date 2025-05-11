package imagecompression;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import javax.imageio.ImageIO;

public class ImageCompression {

    static int arrayLen = 256;
    static int paletteNumber = 8;
    static int char256 = 0;
    static int otherChars = 0;
    static int bitNumberChar256 = 0;
    static int bitNumberOtherChars = 0;

    public static void main(String[] args) throws IOException {
//        String textFileName = "lena_green_values.txt";
//        String imageFileName = "lena.bmp";
        String textFileNameR = "segment_1R.txt";
        String textFileNameG = "segment_1G.txt";
        String textFileNameB = "segment_1B.txt";

        String imageFileName2 = "ucak.bmp";

        String writerPathR = "D:\\Projects\\Java Projects\\NetBeansProjects\\ImageCompressionAlgorithmV3\\src\\texts\\sipi\\segments\\babun\\" + textFileNameR;
        FileWriter writerR = new FileWriter(writerPathR);
        String writerPathG = "D:\\Projects\\Java Projects\\NetBeansProjects\\ImageCompressionAlgorithmV3\\src\\texts\\sipi\\segments\\babun\\" + textFileNameG;
        FileWriter writerG = new FileWriter(writerPathG);
        String writerPathB = "D:\\Projects\\Java Projects\\NetBeansProjects\\ImageCompressionAlgorithmV3\\src\\texts\\sipi\\segments\\babun\\" + textFileNameB;
        FileWriter writerB = new FileWriter(writerPathB);

        File file = new File("D:\\Projects\\Java Projects\\NetBeansProjects\\ImageCompressionAlgorithmV3\\src\\images\\photos\\sipi\\" + imageFileName2);
        BufferedImage img = ImageIO.read(file);

        int height = img.getHeight();
        int width = img.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Alpha      Red        Green      Blue
                // 1111 1111, 1111 1111, 1111 1111, 1111 1111
                int pixel = img.getRGB(x, y);

                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                // Eğer piksel beyaz değilse (RGB değerleri 255 değilse)
                if (!(red == 255 && green == 255 && blue == 255)) {

                    char redChar = (char) red;
                    char greenChar = (char) green;
                    char blueChar = (char) blue;
                    writerR.write(redChar);
                    writerG.write(greenChar);
                    writerB.write(blueChar);
                }
            }
        }
        writerR.close();
        writerG.close();
        writerB.close();
        System.out.println("red values at each pixel are stored txt file.");

        String[] paths = {writerPathR, writerPathG, writerPathB};
        int currentColorByte = 0;
        int totalColorByte = 0;
        for (String currentPath : paths) {
            System.out.println("path:" + currentPath);

            String content = "";
            String fileRPath = currentPath;

            try {
                content = Files.readString(Path.of(fileRPath));
                System.out.println("file readed inside try");
            } catch (IOException e) {
                System.out.println("file read error");
                e.printStackTrace();
            }
            System.out.println("content =");
            System.out.println("content len = " + content.length());

            HashMap<Character, HuffmanNode[]> huffmanAlphabet = letterFrequencyAfterLetterWithHuffman(content);

//        System.out.println("----------- printing char, huffmanNode array -----------");
//        printHashMap(huffmanAlphabet);
            System.out.println("-------------- Sorting ----------");
            huffmanAlphabet.entrySet().forEach(entry -> {
                HuffmanNode[] nodes = entry.getValue();
                if (nodes != null) { // Check if nodes array is not null
                    // Sort in descending order based on item count
                    Arrays.sort(nodes, Comparator.comparingInt(node -> (node != null) ? -node.item : 0));
                    huffmanAlphabet.put(entry.getKey(), nodes);
                }
            });

//        printHashMap(huffmanAlphabet);
            System.out.println("xxxxxxxxxxxxxxxxxxxxx");
//        for (char c : huffmanAlphabet.keySet()) {
//            // 0 54 arası ve 55 degeri yok 54 var ve 55 den gerisi 255e kadar hepsi var
//            System.out.println("c = " + c + " int value of c = " + (int) c + " dictionary value len = " + huffmanAlphabet.get(c).length);
//        }
            System.out.println("map len = " + huffmanAlphabet.size());

            // Yeni yazılacak dosya
            String resultFilePath = "D:\\Projects\\Java Projects\\NetBeansProjects\\ImageCompressionAlgorithmV3\\src\\texts\\results.txt";
            FileWriter resultWriter = new FileWriter(resultFilePath);

            int smallestEncodedBitNumber = Integer.MAX_VALUE;
            int smallestPaletteNumber = 257;

            int counter = 1;
            int minLogResult = Integer.MAX_VALUE;
            // from here ...
            for (; paletteNumber < arrayLen - 1; paletteNumber++) {
                HashMap<Character, HuffmanNode[]> firstFifteenValue = selectFirstFifteenValue(huffmanAlphabet);
                int logResult = calculateLog(firstFifteenValue);
                if (logResult < minLogResult) {
                    minLogResult = logResult;
                    smallestPaletteNumber = paletteNumber;
                    System.out.println("smallest pallette = " + smallestPaletteNumber);
                }
            }
            paletteNumber = smallestPaletteNumber;
            //paletteNumber = 12;
            HashMap<Character, HuffmanNode[]> firstFifteenValue = selectFirstFifteenValue(huffmanAlphabet);
            printHashMap(firstFifteenValue);
            HashMap<Character, HuffmanNode[]> remainingValues = selectRemainingValues(huffmanAlphabet);
            System.out.println("printing remaining values --------------------");
            printHashMap(remainingValues);
            PriorityQueue<HuffmanNode>[] q = createHuffmanTreeWholeLetter(firstFifteenValue);
            // fixed above
            PriorityQueue<HuffmanNode>[] remainingQ = createHuffmanTreeWholeLetter(remainingValues);
            HuffmanNode[] allNodes = createHuffmanTrees(q);
            HuffmanNode[] allNodesAfter = createHuffmanTrees(remainingQ);
            String inputString = content;
            String encodedString = encodeStringV2(inputString, allNodes, allNodesAfter);
            System.out.println("selected palette = " + paletteNumber + "encoded bit = " + encodedString.length());
            currentColorByte = encodedString.length() / 8;
            totalColorByte += currentColorByte;
            System.out.println("encoded byte=" + currentColorByte);
//
//        double averageBitSecondPart = 0.0;
//        if (char256 != 0) {
//            averageBitSecondPart = (bitNumberChar256 / char256 * 1.0);
//        }
//        resultWriter.write(counter + "," + encodedString.length() + "," + (encodedString.length() / 8) + "," + paletteNumber + "," + otherChars + "," + bitNumberOtherChars + "," + (bitNumberOtherChars * 1.0 / otherChars) + "," + char256 + "," + bitNumberChar256 + "," + averageBitSecondPart + "\n");
//
//        if (encodedString.length() < smallestEncodedBitNumber) {
//            smallestEncodedBitNumber = encodedString.length();
//            smallestPaletteNumber = paletteNumber;
//        }
//        counter++;
//
//        resultWriter.close();
//        System.out.println("smallest encoded bit : " + smallestEncodedBitNumber);
//        System.out.println("smallest palette Number: " + smallestPaletteNumber);
//        HashMap<Character, HuffmanNode[]> firstFifteenValue = selectFirstFifteenValue(huffmanAlphabet);
//        System.out.println("first fifteen Value -------------- ");
//        printHashMap(firstFifteenValue);

//        HashMap<Character, HuffmanNode[]> remainingValues = selectRemainingValues(huffmanAlphabet);
//        System.out.println("after fifteen value --------------");
//        printHashMap(remainingValues);
            // error begins...
//        PriorityQueue<HuffmanNode>[] q = createHuffmanTreeWholeLetter(firstFifteenValue);
//        // fixed above
//        PriorityQueue<HuffmanNode>[] remainingQ = createHuffmanTreeWholeLetter(remainingValues);
//        HuffmanNode[] allNodes = createHuffmanTrees(q);
//        for (int i = 0; i < allNodes.length; i++) {
//            System.out.println("------------ " + (char) i + " ascii code is = " + i + " -------------------");
//            printCode(allNodes[i], "");
//        }
//        System.out.println("all nodes len=" + allNodes.length);
//        HuffmanNode[] allNodesAfter = createHuffmanTrees(remainingQ);
//        for (int i = 0; i < allNodesAfter.length; i++) {
//            System.out.println("------------ " + (char) i + " ascii code is = " + i + " -------------------");
//            printCode(allNodesAfter[i], "");
//        }
//        System.out.println("all nodes after len=" + allNodesAfter.length);
            //String inputString = content;
            System.out.println("-------------");
            //String encodedStringFirst = encodeString(inputString, allNodes);
//        String encodedString = encodeStringV2(inputString, allNodes, allNodesAfter);
            System.out.println("encoded.");
//        System.out.println("encoded len = " + encodedString.length());
//        System.out.println("encoded first = " + encodedStringFirst.length());
            //paletteNumber = 8;
        }
        System.out.println("total color byte = " + totalColorByte);
    }

    // harften sonra gelen harflerin sıklığını bulur
    // indis sayısı ascii değerleridir. (a sayısından sonra b geldi diyelim 98 numaralı indisin sayısını 1 artırır)
    //                                  (key value a, bir çok harf gelebilir bundan sonra)
    public static HashMap<Character, HuffmanNode[]> letterFrequencyAfterLetterWithHuffman(String str) {
        HashMap<Character, HuffmanNode[]> freq = new HashMap<>();
        int len = str.length();

        for (int i = 0; i < len - 1; i++) {
            char currentChar = str.charAt(i);
            char nextChar = str.charAt(i + 1);

            freq.putIfAbsent(currentChar, new HuffmanNode[256]);
            HuffmanNode[] nodes = freq.get(currentChar);

            int index = nextChar; // Use ASCII value as index
            if (nodes[index] == null) {
                nodes[index] = new HuffmanNode(1, nextChar);    // initialize with 1
            } else {
                nodes[index].item++;
            }
        }
        return freq;
    }

    public static int calculateLog(HashMap<Character, HuffmanNode[]> huffmanAlphabet) {
        int result = 0;
        int totalFirstFreq = 0;
        int totalSecondFreq = 0;
        for (char currentKey : huffmanAlphabet.keySet()) {
            for (int i = 0; i < huffmanAlphabet.get(currentKey).length - 1; i++) {
                if (huffmanAlphabet.get(currentKey)[i] != null) {
                    totalFirstFreq += huffmanAlphabet.get(currentKey)[i].item;
                }
            }
            totalSecondFreq += huffmanAlphabet.get(currentKey)[huffmanAlphabet.get(currentKey).length - 1].item;
        }
        //System.out.println("first freq = " + totalFirstFreq + "---- second freq = " + totalSecondFreq);
        double log2a = Math.log(paletteNumber) / Math.log(2);
        double log2b = Math.log(255 - paletteNumber) / Math.log(2);

        double resultd = log2a * (totalFirstFreq + totalSecondFreq * log2b);

        result = (int) Math.round(resultd);
        return result;
    }

    public static HashMap<Character, HuffmanNode[]> selectFirstFifteenValue(HashMap<Character, HuffmanNode[]> huffmanAlphabet) {
        System.out.println("selecting first fifteen value");
        int arrayLength = paletteNumber;
        int lastIndex = arrayLength - 1; // this index store sum of value of other characters and assign c as char(256) ascii
        HashMap<Character, HuffmanNode[]> firstFifteenValueofAlphabet = new HashMap<>();

        for (char currentKey : huffmanAlphabet.keySet()) {
            HuffmanNode[] currentKeyFifteenValues = new HuffmanNode[arrayLength];
            currentKeyFifteenValues[lastIndex] = new HuffmanNode(0, ((char) 256));

            int index = 0;
            for (HuffmanNode currentNode : huffmanAlphabet.get(currentKey)) {
                if (currentNode == null) {
                    break;
                }
                if (index == lastIndex) {
                    int value = currentNode.item;
                    currentKeyFifteenValues[lastIndex].item += value;
                } else {
                    //currentKeyFifteenValues[index] = currentNode;
                    currentKeyFifteenValues[index] = new HuffmanNode(currentNode.item, currentNode.c);
                    index++;
                }
            }
            firstFifteenValueofAlphabet.put(currentKey, currentKeyFifteenValues);
        }
        return firstFifteenValueofAlphabet;
    }

    public static HashMap<Character, HuffmanNode[]> selectRemainingValues(HashMap<Character, HuffmanNode[]> huffmanAlphabet) {
        System.out.println("selecting remaining values for " + paletteNumber + "array len = " + arrayLen);
        //int arrayLength = arrayLen - paletteNumber - 1;
        int beginIndex = paletteNumber - 1;

        HashMap<Character, HuffmanNode[]> remainingValuesofAlphabet = new HashMap<>();

        for (char currentKey : huffmanAlphabet.keySet()) {
            HuffmanNode[] currentKeyNodes = new HuffmanNode[256];

            int index = 0;
            for (HuffmanNode currentNode : huffmanAlphabet.get(currentKey)) {
                if (currentNode == null) {
                    break;
                }
                if (index < beginIndex) {
                    index++;
                    continue;
                } else {
                    currentKeyNodes[index] = currentNode;
                    remainingValuesofAlphabet.put(currentKey, currentKeyNodes);
                    index++;
                }
            }
        }
        return remainingValuesofAlphabet;
    }

    public static PriorityQueue<HuffmanNode>[] createHuffmanTreeWholeLetter(HashMap<Character, HuffmanNode[]> map) {
        // n should be 256 and if there is no element that ascii code index of array should be null 
        int n = 256;
        //PriorityQueue<HuffmanNode>[] sortedQ = new PriorityQueue<> (n, new ImplementComparator());
        PriorityQueue<HuffmanNode>[] sortedQ = (PriorityQueue<HuffmanNode>[]) new PriorityQueue[n];

        for (int i = 0; i < n; i++) {
            sortedQ[i] = new PriorityQueue<>(new ImplementComparator());
        }
        int index = 0;

        for (char c : map.keySet()) {
            for (HuffmanNode currentNode : map.get(c)) {
                if (currentNode != null) {
                    index = (int) c;
                    sortedQ[index].add(currentNode);
                }
            }
        }
        return sortedQ;     // debug
    }

    public static HuffmanNode[] createHuffmanTrees(PriorityQueue<HuffmanNode>[] q) {
        int len = q.length;
        HuffmanNode[] allNodes = new HuffmanNode[len];
        for (int i = 0; i < len; i++) {
            HuffmanNode root = null;
            while (q[i].size() > 1) {
                HuffmanNode x = q[i].peek();
                q[i].poll();

                HuffmanNode y = q[i].peek();
                q[i].poll();

                HuffmanNode f = new HuffmanNode();
                f.item = x.item + y.item;
                f.c = '-';
                f.leftNode = x;
                f.rightNode = y;
                root = f;

                q[i].add(f);
            }
            allNodes[i] = root;
            //printCode(root, "");
            //System.out.println("------------------");
        }
        return allNodes;
    }

    public static String encodeString(String inputString, HuffmanNode[] allNodes) {
        StringBuilder result = new StringBuilder(); // Using StringBuilder for better performance
        int len = inputString.length();

        int externalCounter = 0;

        int counter = 0;
        // Encode the first character's ASCII value in binary (8 bits)
        char firstChar = inputString.charAt(0);
        result.append(get8BitBinaryString(firstChar));

        for (int i = 1; i < len; i++) {
            int treeToSearch = inputString.charAt(i - 1);
            HuffmanNode currentNode = allNodes[treeToSearch];

            char currentChar = inputString.charAt(i);
            String currentCharHuffmanCode = findHuffmanCode(currentNode, currentChar, "");

            if (currentCharHuffmanCode == null) {

                // Add Huffman code for '*' (256)
                String starCode = findHuffmanCode(currentNode, (char) 256, "");
                result.append(starCode);
                // Add the ASCII value of the character in binary (8 bits)
                result.append(get8BitBinaryString(currentChar));
                counter++;
            } else {
                // Append the Huffman code for the current character
                result.append(currentCharHuffmanCode);
                externalCounter++;
            }
        }

        System.out.println("counter of char 256= " + counter);
        System.out.println("counter of other chars = " + externalCounter);
        return result.toString();
    }

    public static String encodeStringV2(String inputString, HuffmanNode[] allNodes, HuffmanNode[] remainingNodes) {
        StringBuilder result = new StringBuilder(); // Using StringBuilder for better performance
        int len = inputString.length();

        bitNumberChar256 = 0;
        bitNumberOtherChars = 0;

        int externalCounter = 0;

        int counter = 0;
        // Encode the first character's ASCII value in binary (8 bits)
        char firstChar = inputString.charAt(0);
        result.append(get8BitBinaryString(firstChar));

        for (int i = 1; i < len; i++) {
            int treeToSearch = inputString.charAt(i - 1);
            HuffmanNode currentNode = allNodes[treeToSearch];

            char currentChar = inputString.charAt(i);
            String currentCharHuffmanCode = findHuffmanCode(currentNode, currentChar, "");

            if (currentCharHuffmanCode == null) {

                // Add Huffman code for '*' (256)
                String starCode = findHuffmanCode(currentNode, (char) 256, "");
                result.append(starCode);
                // Add the ASCII value of the character in binary (8 bits)
                //result.append(get8BitBinaryString(currentChar));
                HuffmanNode currentRemainingNode = remainingNodes[treeToSearch];
                String currentRemainingCharHuffmanCode = findHuffmanCode(currentRemainingNode, currentChar, "");
                result.append(currentRemainingCharHuffmanCode);
                counter++;
                if (currentRemainingCharHuffmanCode != null) {
                    bitNumberChar256 += (currentRemainingCharHuffmanCode.length());
                }
            } else {
                // Append the Huffman code for the current character
                result.append(currentCharHuffmanCode);
                externalCounter++;
                bitNumberOtherChars += currentCharHuffmanCode.length();
            }
        }

        char256 = counter;
        otherChars = externalCounter;
        System.out.println("counter of char 256= " + counter + "- char256=" + char256);
        System.out.println("counter of other chars = " + externalCounter + "- otherChars=" + otherChars);
        return result.toString();
    }

    public static String findHuffmanCode(HuffmanNode root, char target, String code) {
        if (root == null) {
            return null;
        }
        if (root.c == target) {
            return code;
        }
        // Search left subtree first
        String left = findHuffmanCode(root.leftNode, target, code + "0");
        if (left != null) {
            return left;
        }
        // Then search right subtree
        return findHuffmanCode(root.rightNode, target, code + "1");
    }

    private static String get8BitBinaryString(char c) {
        String binaryString = Integer.toBinaryString(c & 0xFF);
        // Pad binary string with leading zeros if necessary
        return "00000000".substring(binaryString.length()) + binaryString;
    }

    public static void printCode(HuffmanNode root, String s) {
        if (root == null) {
            return;
        }
        if (root.leftNode == null && root.rightNode == null) {
            System.out.println(root.c + "   |   " + s);
            return;
        }
        printCode(root.leftNode, s + "0");
        printCode(root.rightNode, s + "1");
    }

    public static void printHashMap(HashMap<Character, HuffmanNode[]> huffmanAlphabet) {

        for (char key : huffmanAlphabet.keySet()) {
            System.out.print(key + ": ");
            HuffmanNode[] nodes = huffmanAlphabet.get(key);
            for (HuffmanNode node : nodes) {
                if (node != null) {
                    System.out.print("(" + node.c + ", " + node.item + ")");
                }
            }
            System.out.println();
        }
    }
}

class HuffmanNode {

    int item;
    char c;
    HuffmanNode rightNode;
    HuffmanNode leftNode;

    public HuffmanNode() {
    }

    public HuffmanNode(int item, char c) {
        this.item = item;
        this.c = c;
    }
}

class ImplementComparator implements Comparator<HuffmanNode> {

    @Override
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.item - y.item;
    }
}