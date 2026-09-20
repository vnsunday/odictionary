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

	public static void main(String[] args) {

		for (int i=0; i< args.length; i++) {
			System.out.println(String.format("%d: %s", i, args[i]));
		}
		if (args.length < 2){
			System.out.println("Usage1: SoftPronounce filein fileout");
			System.out.println("Usage2: SoftPronounce findmissing file_pronounce filecontent");
			System.out.println("    findmissing: scan text content (filecontent) and find missing words (words which are not defined in file_pronounce). Listing them all.");
			return;
		}
		
		/*============================================================
		 * Process 
		 * 
		 *============================================================*/
		String filepr = args[0];
		String fileout = args[1];
		String filetxt = null;
		String operation = null;
		if (args.length == 3) {
			// Parameter 3
			operation = args[0];
			filepr = args[1];
			filetxt = args[2];
			
			if ("findmissing".compareToIgnoreCase(operation) != 0) {
				System.out.println("Parameters incorrect");
				return;
			}
		}
		
		/*============================================================
		 * 
		 *============================================================*/
		char letter;
		List<String[]> dict = new ArrayList<String[]>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filepr))) {
			String line;
			String word;
			
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
							word = line.substring(0, nC).trim();							
						}
						else {
							word = line.substring(0, nB).trim();
						}
					} else {
						word = line.trim(); // Accept words without a pronounce (for other operations). eg a plural form of a word: episodes					
					}
					
					dict.add(new String[] { word.replaceAll("\\.", "").toLowerCase(), line });  // Word can contains separator (the dot .). For example: sec.tor
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
