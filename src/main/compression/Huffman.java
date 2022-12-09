package main.compression;

import java.util.*;
import java.util.Map.Entry;
import java.io.ByteArrayOutputStream; // Optional

/**
 * Huffman instances provide reusable Huffman Encoding Maps for
 * compressing and decompressing text corpi with comparable
 * distributions of characters.
 */
public class Huffman {
    
    // -----------------------------------------------
    // Construction
    // -----------------------------------------------

    private HuffNode trieRoot;
    // TreeMap chosen here just to make debugging easier
    private TreeMap<Character, String> encodingMap;
    // Character that represents the end of a compressed transmission
    private static final char ETB_CHAR = 23;
    
    /**
     * Creates the Huffman Trie and Encoding Map using the character
     * distributions in the given text corpus
     * 
     * @param corpus A String representing a message / document corpus
     *        with distributions over characters that are implicitly used
     *        throughout the methods that follow. Note: this corpus ONLY
     *        establishes the Encoding Map; later compressed corpi may
     *        differ.
     */
    public Huffman (String corpus) {
    	Map<Character, Integer> characterandFrequency = new HashMap<>();
    	characterandFrequency.put(ETB_CHAR, 1);
    	for (int i=0; i < corpus.length(); i++) {
    		if (characterandFrequency.containsKey(corpus.charAt(i))) {
    			characterandFrequency.put(corpus.charAt(i), characterandFrequency.get(corpus.charAt(i))+1);
    		}
    		else {
    			characterandFrequency.put(corpus.charAt(i), 1); 
    		}	
    	}
    	PriorityQueue<HuffNode> huffNodes = addToQueue(characterandFrequency);
    	this.addToTrie(huffNodes);
    	this.encodingMap = new TreeMap<>();
    	this.addToEncodingMap(this.trieRoot, "");	
    }
    
    // -----------------------------------------------
    // Compression
    // -----------------------------------------------
    /**
     * Compresses the given String message / text corpus into its Huffman coded
     * bitstring, as represented by an array of bytes. Uses the encodingMap
     * field generated during construction for this purpose.
     * 
     * @param message String representing the corpus to compress.
     * @return {@code byte[]} representing the compressed corpus with the
     *         Huffman coded bytecode. Formatted as:
     *         (1) the bitstring containing the message itself, (2) possible
     *         0-padding on the final byte.
     */
    public byte[] compress (String message) {
    	String bitString = "";
        for(int i = 0; i < message.length(); i++) {
        	bitString += this.encodingMap.get(message.charAt(i));
        }
        bitString += this.encodingMap.get(ETB_CHAR);
        return changeTobyte(bitString);  
    }
    
    // -----------------------------------------------
    // Decompression
    // -----------------------------------------------
    
    /**
     * Decompresses the given compressed array of bytes into their original,
     * String representation. Uses the trieRoot field (the Huffman Trie) that
     * generated the compressed message during decoding.
     * 
     * @param compressedMsg {@code byte[]} representing the compressed corpus with the
     *        Huffman coded bytecode. Formatted as:
     *        (1) the bitstring containing the message itself, (2) possible
     *        0-padding on the final byte.
     * @return Decompressed String representation of the compressed bytecode message.
     */
    public String decompress (byte[] compressedMsg) {
    	String decodedCharacters = "";
    	HuffNode current = this.trieRoot;
    	String compressedString = "";
    	for(int j = 0; j< compressedMsg.length; j++) {
    		String compressedByteString = Integer.toBinaryString(compressedMsg[j] & 0xff);
    		while(compressedByteString.length() %8 != 0) {
    			compressedByteString = "0" + compressedByteString;
    		}
    		compressedString += compressedByteString;	
    	}
    	for(int i = 0; i < compressedString.length(); i ++) {
    		if(current.isLeaf()) {
		// >> [KT] Some spacing inconsistencies here (-0.5)
				if(current.character == ETB_CHAR) {
					return decodedCharacters;
				}
				else {
					decodedCharacters += current.character;
					current = this.trieRoot;
					i --;
				}
    		}
    		else {
    			if(compressedString.charAt(i) == '0') {
    				current = current.zeroChild;
    				
    			}
    			else {
    				current = current.oneChild;
    			}
    		}
    	}
        return decodedCharacters;
    }
    
    
    // -----------------------------------------------
    // Huffman Trie
    // -----------------------------------------------
    
    /**
     * Huffman Trie Node class used in construction of the Huffman Trie.
     * Each node is a binary (having at most a left (0) and right (1) child), contains
     * a character field that it represents, and a count field that holds the 
     * number of times the node's character (or those in its subtrees) appear 
     * in the corpus.
     */
    private static class HuffNode implements Comparable<HuffNode> {
        
        HuffNode zeroChild, oneChild;
        char character;
        int count;
        
        HuffNode (char character, int count) {
            this.count = count;
            this.character = character;
        }
        
        public boolean isLeaf () {
            return this.zeroChild == null && this.oneChild == null;
        }
        
        public int compareTo (HuffNode other) {
            
        	if(this.count == other.count) {
        		return this.character - other.character;
        	}
        	return this.count - other.count;
        }
        
    }
    // -----------------------------------------------
    // All helped methods used are below.
    // -----------------------------------------------
    
    /**
     * Helper method to add to our encoding map
     * @param node : the node we are checking and adding the character in that node
     * to our encoding map. 
     * @param bitString : a collection of our Character's bit strings (0's and 1's).
     */
    private void addToEncodingMap(HuffNode node, String bitString) {
    	if(node.isLeaf()) {
    		this.encodingMap.put(node.character, bitString);
    		return;
    	}
    	else {
    		addToEncodingMap(node.zeroChild, bitString + "0");
    		addToEncodingMap(node.oneChild, bitString + "1");		 
    	}
    }
    
    /**
     * Helper method to add to our map to a priority queue.
     * @param map : our map of Character to Integer.
     * @return the priority queue we just made.
     */
    private static PriorityQueue<HuffNode> addToQueue(Map<Character, Integer> map) {
    	PriorityQueue<HuffNode> queue = new PriorityQueue<HuffNode>();
    	for (Map.Entry<Character, Integer> entry : map.entrySet()) {
    		HuffNode prioritynode = new HuffNode(entry.getKey(), entry.getValue());
    		queue.add(prioritynode);
    	}
    	return queue; 
    }
    
    /**
     * Helper method to add our queue to a trie.
     * @param queue : the queue we want to add to the trie.
     */
    private void addToTrie(PriorityQueue<HuffNode> queue) {
    	//This makes the trie.
    	while(queue.size() > 1) {
    		HuffNode poppedFirst = queue.poll(); 
    		HuffNode poppedSecond = queue.poll();
    		HuffNode parent = new HuffNode(poppedFirst.character, poppedFirst.count + poppedSecond.count);
    		queue.add(parent);
    		//This connects the parent node to its edges
    		parent.zeroChild = poppedFirst;
    		parent.oneChild = poppedSecond;	
    	}
    	this.trieRoot = queue.poll();	
    }
    
    /**
     * Helper method to change a string to a byte
     * @param word : the string we want to represent in bytes
     * @return an Array with bytes that represent word
     */
    // >> [KT] Great use of helper methods 
    private byte[] changeTobyte(String word) {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	while(word.length()%8 != 0) {
    		word+= "0";
    	}
    	for(int i = 0; i < word.length(); i+= 8) {
    		String bits = word.substring(i, i + 8); 
    		int parse = Integer.parseInt(bits, 2); 
    		output.write((byte) parse);
    	}
    	return output.toByteArray();
    }

}

// ===================================================
// >>> [KT] Summary
// This is a very solid submission both from a style 
// and a logic perspective. Great use of helper methods, 
// good variable and method names, and overall an 
// implementation that shows off your understanding of 
// compression algorithms. Nicely done! 
// ---------------------------------------------------
// >>> [KT] Style Checklist
// [X] = Good, [~] = Mixed bag, [ ] = Needs improvement
//
// [X] Variables and helper methods named and used well
// [X] Proper and consistent indentation and spacing
// [X] Proper JavaDocs provided for ALL methods
// [X] Logic is adequately simplified
// [X] Code repetition is kept to a minimum
// ---------------------------------------------------
// Correctness:          98.5 / 100 (-1.5 / missed unit test)
// Style Penalty:       -0.5
// Total:                98 / 100
// ===================================================
