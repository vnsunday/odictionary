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

	public static void main(String[] args) {

		for (int i=0; i< args.length; i++) {
			System.out.println(String.format("%d: %s", i, args[i]));
		}
		if (args.length < 2){
			System.out.println("Usage: filein fileout");
			return;
		}
		
		/*============================================================
		 * 
		 *============================================================*/
		String filepr = args[0];
		String fileout = args[1];
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
							word = line.substring(0, nC);							
						}
						else {
							word = line.substring(0, nB);
						}
						dict.add(new String[] { word, line });
					}
				}
			}
			
			System.out.println(String.format("Finished reading. WordCount=%d", dict.size()));
			Collections.sort(dict, new  Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					return o1[0].toLowerCase().compareTo(o2[0].toLowerCase());
				}
			});
			
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
			
			System.out.println("FINISH");
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
