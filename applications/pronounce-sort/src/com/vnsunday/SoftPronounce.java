package com.vnsunday;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SoftPronounce {
	
	static void loadFileText(String file, List<String> words) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(file));
		
		String line;
		while ( (line = rd.readLine()) != null) {
			String[] azw = line.split("[,; \\.\\?]+");
			
			for (int i = 0; i< azw.length; i++) {
				int index = Collections.binarySearch(words, azw[i].toLowerCase());
				
				if (index < 0) {
					index = - (index + 1);
					words.add(index, azw[i].toLowerCase());
				}				
			}
		}
		rd.close();
	}
	
	static void text_to_pronounce(List<String[]> dictionary, String text, String fileout) throws IOException {
		/*============================================================
		 * Method:
		 *     Split to Token(Words|Punctations)
		 *     
		 * If a Token in Directory => replace Token to Dictionary Pronounciation
		 * 		If The Pronounciation is complex (containing multiples description)
		 * 			=> Add Parentheses
		 * If a Token not in Directory => Keep
		 *============================================================*/
		
		String[] words = text.split("[ \t]+");
		String[] outs = new String[words.length];
		String pronounce;

		Comparator<String[]> wordComp = Comparator.comparing( (String[] u) -> u[0]);
		for (int i=0; i<words.length;i++) {
			int index = Collections.binarySearch(dictionary, new String[] { words[i].toLowerCase() }, wordComp);
			int countSlash = 0;
			
			if (index >= 0) {
				pronounce = dictionary.get(index)[2];
				
				// Null Pronounciation => Keep
				if (pronounce == null || pronounce.isEmpty()) {
					outs[i] = words[i];
				}
				else {
					countSlash = 0;
					for (int j=0; j< pronounce.length(); j++) {
						if (pronounce.charAt(j) == '/') {
							countSlash++;
						}
					}
					
					// Complex (multiple description)? => Add Parentheses
					if (countSlash > 2 || pronounce.charAt(0) != '/' || pronounce.charAt(pronounce.length()-1) != '/' ) {
						outs[i] = "(" + pronounce + ")";
					}
					else {
						outs[i] = pronounce;
					}
				}
			}
			// Not In dictionary => Keep
			else {
				outs[i] = words[i];
			}
		}
		
		// Write to output file
		BufferedWriter wr = Files.newBufferedWriter(Paths.get(fileout));
		for (int i=0; i<outs.length; i++) {
			if (i > 0) {
				wr.write(" ");
			}
			wr.write(outs[i]);
		}
		wr.close();
	}

	public static void main(String[] args) {

		for (int i=0; i< args.length; i++) {
			System.out.println(String.format("%d: %s", i, args[i]));
		}
		if (args.length < 2){
			System.out.println("Usage1: SoftPronounce filedictionary fileout");
			System.out.println("    Sort words of filedictionary in ascending order and save to fileout");
			System.out.println("Usage2: SoftPronounce findmissing file_pronounce_dictionary filecontent");
			System.out.println("    findmissing: scan text content (filecontent) and find missing words (words which are not defined in file_pronounce_dictionary). Listing them all.");
			System.out.println("Usage 3: SoftPronounce create file_pronounce_dictionary filecontent fileoutput");
			System.out.println("    create: create Pronounciation file from filecontent, replace words to pronounciation; keep punctuation marks. Save the output the fileoutput");
			return;
		}
		
		/*============================================================
		 * Process 
		 * 
		 *============================================================*/
		String file_dict = args[0];
		String fileout = args[1];
		String filetxt = null;
		
		String operation = null;
		if (args.length == 3) {
			// Parameter 3
			operation = args[0];
			file_dict = args[1];
			filetxt = args[2];
			
			if ("findmissing".compareToIgnoreCase(operation) != 0) {
				System.out.println("Parameters incorrect");
				return;
			}
			
		}
		else if (args.length == 4) {
			operation = args[0];
			file_dict = args[1];
			filetxt = args[2];
			fileout = args[3];
			
			if ("create".compareToIgnoreCase(operation) != 0) {
				System.out.println("Parameters incorrect");
				return;
			}
		}
		
		/*============================================================
		 * 
		 *============================================================*/
		char letter;
		List<String[]> dict = new ArrayList<String[]>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file_dict))) {
			String line;
			String word;
			String pronounce;
			
			while ((line = br.readLine()) != null) {
				// Parsing
				if (line.isEmpty()) {
				}
				else if (line.charAt(0) == '#') {
					// Skip
				}
				else {
					int nB = line.indexOf('/'); // Find the beginning of pronounce; eg /ei/
					int nC = line.indexOf(':'); // Find the separator if there are many pronounces 
									
					if (nB >= 0) {
						if (nC >= 0 && nC < nB) {
							word = line.substring(0, nC).trim();   // Remove word seperator e.g sec.tor => sector
							pronounce = line.substring(nC).trim();
						}
						else {
							word = line.substring(0, nB).trim();
							pronounce = line.substring(nB).trim();
						}
					} else {
						word = line.trim(); // Accept words without a pronounce (for other operations). eg a plural form of a word: episodes
						pronounce = null;
					}
					dict.add(new String[] { word.replaceAll("\\.", "").toLowerCase(), line, pronounce });  // Word can contains separator (the dot .). For example: sec.tor
				}
			}
			
			System.out.println(String.format("Finished reading. WordCount=%d", dict.size()));
			Collections.sort(dict, new  Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					return o1[0].toLowerCase().compareTo(o2[0].toLowerCase());
				}
			});
			
			// OP1. Sorting
			if (operation == null) {
				// Write to the output File
				letter = ' ';
				BufferedWriter wr = Files.newBufferedWriter(Paths.get(fileout));
				for (int i=0; i<dict.size(); i++) {
					
					char ch = Character.toUpperCase( dict.get(i)[0].charAt(0));
					
					if (ch != letter) {
						letter = ch;
						wr.write(String.format("# %c", letter));
						wr.newLine();
					}
					wr.write(dict.get(i)[1]);
					wr.newLine();
				}
				wr.close();
				br.close();
				
				System.out.println("FINISH SORTING");
			}
			else if ("findmissing".compareToIgnoreCase(operation) == 0) {
				// Find 
				List<String> textWords = new ArrayList<String>();
				loadFileText(filetxt, textWords);
				System.out.println(String.format("Load Context: %d", textWords.size()));
				
				Comparator<String[]> bookComp = Comparator.comparing( (String[] u) -> u[0]);
				
				for (int i=0; i< textWords.size(); i++) {
					int index = Collections.binarySearch(dict, new String[] {textWords.get(i), ""}, bookComp);
					
					if (index >= 0) {
						// System.out.println(String.format("%s FOUND", textWords.get(i)));
					}
					else {
						System.out.println(textWords.get(i));
					}
				}
				
				System.out.println("FINISH find missing");
			}
			else if ("create".compareToIgnoreCase(operation) == 0) {
				String textcontent = "";
				
				BufferedReader rd = new BufferedReader(new FileReader(filetxt));
				while ( (line = rd.readLine()) != null) {
					textcontent += line + System.lineSeparator();
				}
				rd.close();
				
				text_to_pronounce(dict, textcontent, fileout);
			}
			else {
				System.out.println("Parameters are not correct.");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
		}
	}

}
