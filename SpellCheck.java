import java.io.*;
import java.util.*;


public class SpellCheck {

	/**
	 * @param args
	 * @throws IOException 
	 */
	static HashMap<Integer, List<String>> dictionary = new HashMap<Integer, List<String>>();
	static List<Character> vowels = new ArrayList<Character>();
	static final String noSuggestions = "NO SUGGESTION";
	
	public static void main(String[] args) throws IOException {	
		vowels.add('a');
		vowels.add('e');
		vowels.add('i');
		vowels.add('o');
		vowels.add('u');
		
		populateDictionary();
		
		System.out.println("(Enter \"quit\" to exit)");
		//keep looping for user input
		while(true) {
			System.out.print("> ");
			Scanner scanner = new Scanner(System.in);
			String inputWord = scanner.next().trim();
			if(inputWord.equalsIgnoreCase("quit")) {
				break;
			}
			String intactInputCopy = new String(inputWord); 
			inputWord = inputWord.toLowerCase();
			
			//get list of words that have the same core as the input word
			int inputCoreHash = getCore(inputWord).hashCode();
			List<String> list = dictionary.get(inputCoreHash);
			
			//if there isn't one, then we're done
			if(list == null) {
				System.out.println(noSuggestions);
				continue;
			} else if(list.size() == 1) { //if list has only one word, then that's the only possible suggestion
				System.out.println(list.get(0));
				continue;
			}
			
			//out of words with same core, find those with same consonant-vowel pattern
			String inputPattern = getPattern(inputWord);
			List<String> listMatched = new ArrayList<String>();
			for(String word : list) {
				if(getPattern(word).equals(inputPattern)) {
					listMatched.add(word);
				}
			}
			
			//if zero, then done; if one, then only possible suggestion
			if(listMatched.size() == 0) {
				System.out.println(noSuggestions);
				continue;
			}
			else if(listMatched.size() == 1) {
				System.out.println(listMatched.get(0));
				continue;
			}
			
			//find exact matches, case insensitive
			List<String> newMatches = new ArrayList<String>();
			for(String word : listMatched) {
				if(word.equalsIgnoreCase(intactInputCopy)) {
					newMatches.add(word);
				}
			}
			
			//if there are matches by exact spelling
			if(newMatches.size() == 1) {
				//only possibility
				System.out.println(newMatches.get(0));
				continue;
			} else if(newMatches.size() == 2) {
				//find the one that matches by case, otherwise just pick one
				for(String word : newMatches) {
					if(word.equals(intactInputCopy)) {
						System.out.println(word);
						continue;
					} else {
						System.out.println(newMatches.get(0));
						continue;
					}
				}
			} 
			
			//if no exact spelling match, then narrow down by checking vowels
			List<String> newListMatches = new ArrayList<String>();
			for(int i = 0; i < inputWord.length(); i++) {
				char letter = inputWord.charAt(i);
				if(vowels.contains(letter)) {
					for(String w : listMatched) {
						String let = Character.toString(letter);
						if(w.contains(let)) {
							newListMatches.add(w);
						}
					}
				}
			}
			
			int mSize = newListMatches.size();
			if(mSize == 0) {
				//just pick one
				System.out.println(listMatched.get(0));
			} else if(mSize == 1) {
				//got the best one
				System.out.println(newListMatches.get(0));
			} else { 
				//capitalized versions/words first followed by lowercased version/words
				if(intactInputCopy.charAt(0) <= newListMatches.get(0).charAt(0)) {
					//input word was capitalized, so get a capitalized word
					System.out.println(newListMatches.get(0));
				} else {
					//input word was lowercased, so get lowercased word (usually) instead
					System.out.println(newListMatches.get(1));
				}	
			}
		}
	}
	
	public static void populateDictionary() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("english.dict")));
		String line = null;
		
		//iterate through each word in dictionary
		while((line = reader.readLine()) != null) {
			line = line.trim();	

			//make a copy to later store
			String intactCopy = new String(line);		
			//normalize
			line = line.toLowerCase();
			
			//strip duplicate consonants and vowels so that we get the "core" of the word
			String core = getCore(line);
			
			//use this core in order to create a key that points to a collection of words
			//that match this core when stripped down to their core
			int coreHash = core.hashCode();
			//System.out.println("Core: " + core);
			//System.out.println("Intact: " + intactCopy);
			if(!dictionary.containsKey(coreHash)) {
				List<String> list = new ArrayList<String>();
				list.add(intactCopy);
				dictionary.put(coreHash, list);
			} else {
				dictionary.get(coreHash).add(intactCopy);
			}
		}	
		
		reader.close();
	}
	
	public static String getCore(String line) {
		StringBuilder core = new StringBuilder("");
		for(int i = 0; i < line.length(); i++) {
			char letter = line.charAt(i);	
			if(!vowels.contains(letter)) {
				if(i > 0 && (letter != line.charAt(i - 1))) {
					//if not double consonant or vowel, add to core
					core.append(letter);			
				} else if(i == 0){ //always add beginning consonant
					core.append(letter);
				}
			}
		}
		return core.toString();
	}
	
	public static String getPattern(String word) {
		StringBuilder pattern = new StringBuilder("");
		boolean consecutiveC = false;
		boolean consecutiveV = false;
		word = word.toLowerCase(); //so we can ignore case during comparison
		for(int i = 0; i < word.length(); i++) {
			if(!vowels.contains(word.charAt(i))) {
				if(!consecutiveC) {
					pattern.append("c");	
					consecutiveC = true;
					consecutiveV = false;
				} 
			} else {
				if(!consecutiveV) {
					pattern.append("v");
					consecutiveC = false;
					consecutiveV = true;
				}
			}
		}
		return pattern.toString();
	}
}
