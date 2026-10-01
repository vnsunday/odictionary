package com.vnsunday;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	/* is range [start, end-1] inside string with length=len*/
	static String sub_str(int start, int end, String str) {
		return (start >= 0 && end <= str.length() && start < end) ? str.substring(start, end) : "";
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
		
		String[] seps = new String[] { " ", "\t", "\n" }; // 
		String[] tokens = text.split("[ \t]+");
		String[] outs = new String[tokens.length];
		String pronounce;
		String word = null;
		String wtrimL;
		String wtrimR;

		Comparator<String[]> wordComp = Comparator.comparing( (String[] u) -> u[0]);
		for (int i=0; i<tokens.length;i++) {
			
			// Extract the text content 
			// Extract Left & Right Non-Text. e.g punctuations; newline;...  
			// Example "section." => word = section; wtrimL=""; wtrimR = "."
			int l;
			int r;
			int tklen = tokens[i].length();
			l = 0;
			r = tklen - 1;
			while (l < tokens[i].length() && 
					!((tokens[i].charAt(l) >= 'a' && tokens[i].charAt(l) <= 'z') ||
							(tokens[i].charAt(l) >= 'A' && tokens[i].charAt(l) <= 'Z')) ) 
			{
				l++;
			}
			while (r >=0 && 
					!(
						(tokens[i].charAt(r) >= 'a' && tokens[i].charAt(r) <= 'z') || 
						(tokens[i].charAt(r) >= 'A' && tokens[i].charAt(r) <= 'Z')) ) {
				r--;
			}
			
			word = sub_str(l, r+1, tokens[i]).toLowerCase();
			wtrimL = sub_str(0, l, tokens[i]).toLowerCase();
			wtrimR = sub_str(r+1, tklen, tokens[i]).toLowerCase();
			
			// For Debug Only
			String[] debug_tokens = new String[] { "sector", "retail"};
			for (int j=0; j<debug_tokens.length; j++) {
				if (tokens[i].indexOf(debug_tokens[j]) >= 0) {
					System.out.println(String.format("Token=%s; trimL=%s; trimR=%s; word=%s", 
							tokens[i], wtrimL, wtrimR, word
							));
				}
			}
			// For Debug - END
			
			int index = Collections.binarySearch(dictionary, new String[] { word }, wordComp);
			int countSlash = 0;
			
			if (index >= 0) {
				pronounce = dictionary.get(index)[2];
				
				// Null Pronounciation => Keep
				if (pronounce == null || pronounce.isEmpty()) {
					outs[i] = tokens[i];
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
						outs[i] = wtrimL + "(" + pronounce + ")" + wtrimR;
					}
					else {
						outs[i] = wtrimL + pronounce + wtrimR;
					}
				}
			}
			// Not In dictionary => Keep
			else {
				outs[i] = tokens[i];
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

	static void text_to_pronounce_v2(List<String[]> dictionary, String text, String fileout) throws IOException {
		char[] seps = new char[] { ' ', '\t', '\n' };
		
		int i = 0;
		int n = text.length();
		StringBuilder sb = new StringBuilder();
		String token = "";
		String outtoken = "";
		String pronounce;
		String word = null;
		String wtrimL;
		String wtrimR;
		
		
		Comparator<String[]> wordComp = Comparator.comparing( (String[] u) -> u[0]);
		/*============================================================
		 * Scanning Left to Right
		 * 
		 * S1. If (Reach-The-End) => Process the Accumulated-Token => Append The Result
		 * S2. If (Current char IsSeparator)
		 * 				Process the Accumulated-Token. Append The Result
		 * 				Reset Acculated-Token = ""
		 * 				Append Current-char to the Result
		 * S3. If (Current Char IsNot Separator)
		 * 				Append Current-Char to the Accumulated Token					
		 *============================================================*/
		i = 0;
		while (i <= n) {
			char ch = 0;
			boolean isSeparator = false;
			
			if (i < n) {
				ch = text.charAt(i);
				for (int j=0; j<seps.length;j++) {
					if (seps[j] == ch) {
						isSeparator = true;
						break;
					}
				}
			}
			
			// S1 & S2
			//		Reach-The-End (i==n)
			//		Current char IsSeparator (i < n && currentchar)
			// => 
			//		Process The AccumulatedToken. Append the Result
			//		Reset Acculated-Token			
			if ((isSeparator || i == n) && !token.isEmpty()) {
				// Process the Word
				// Extract the text content 
				// Extract Left & Right Non-Text. e.g punctuations; newline;...  
				// Example "section." => word = section; wtrimL=""; wtrimR = "."
				int l;
				int r;
				int tklen = token.length();
				l = 0;
				r = tklen - 1;
				while (l < tklen && 
						!((token.charAt(l) >= 'a' && token.charAt(l) <= 'z') ||
								(token.charAt(l) >= 'A' && token.charAt(l) <= 'Z')) ) 
				{
					l++;
				}
				while (r >=0 && 
						!(
							(token.charAt(r) >= 'a' && token.charAt(r) <= 'z') || 
							(token.charAt(r) >= 'A' && token.charAt(r) <= 'Z')) ) {
					r--;
				}
				
				word = sub_str(l, r+1, token).toLowerCase();
				wtrimL = sub_str(0, l, token).toLowerCase();
				wtrimR = sub_str(r+1, tklen, token).toLowerCase();
				// Append to the Output
				int index = Collections.binarySearch(dictionary, new String[] { word }, wordComp);
				int countSlash = 0;
				
				if (index >= 0) {
					pronounce = dictionary.get(index)[2];
					
					// Null Pronounciation => Keep
					if (pronounce == null || pronounce.isEmpty()) {
						outtoken = token;
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
							outtoken = wtrimL + "(" + pronounce + ")" + wtrimR;
						}
						else {
							outtoken = wtrimL + pronounce + wtrimR;
						}
					}
				}
				// Not In dictionary => Keep
				else {
					outtoken = token;
				}
				sb.append(outtoken);
				
				// Reset current token
				token = "";
			}
			// S2.  IsSeparator => Append the Separator to The-result  (after processing token)
			if (i < n && isSeparator) {
				sb.append(ch);
			}
			
			//S3. Not Separator => Append to the accumulatedToken
			if (i < n && !isSeparator) {
				token += ch;
			}
			i++;
		}
		
		// WRite to the File
		Path path = Paths.get(fileout);
		Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
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
				
				text_to_pronounce_v2(dict, textcontent, fileout);
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
