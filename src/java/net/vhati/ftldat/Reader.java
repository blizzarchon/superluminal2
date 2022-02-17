package net.vhati.ftldat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.kartoflane.superluminal2.utils.IOUtils.DecodeResult;

public class Reader {
	
	public ArrayList<String> name = new ArrayList<String>();
	public ArrayList<ArrayList<String>> aug;
	public ArrayList<Integer> lim = new ArrayList<Integer>();
	File fle;
	String text;
	boolean moreAugs;
	public Reader(DecodeResult text) throws IOException
	{
		this.text = text.text;
		this.fle = Writer(this.text);
		Scanner fileReader = new Scanner(fle);
		String result = "";
		aug = new ArrayList<ArrayList<String>>();
		int count = 0;
		while(fileReader.hasNext())
		{
			result = fileReader.nextLine().trim();
			if(result.startsWith("<customShip name="))
			{
				name.add(result.substring(18, result.length()-2));
				result = fileReader.nextLine().trim();
				moreAugs = true;
				boolean firstAug = true;
				boolean wrongOrder = false;
				while(moreAugs)
				{
					if (result.startsWith("<crewLimit>"))
					{
						wrongOrder = true;
						lim.add(Integer.parseInt(result.substring(11, result.length()-12)));
						result = fileReader.nextLine().trim();
					}
					if (result.startsWith("<hiddenAug>"))
					{
						if (firstAug)
						{
							aug.add(new ArrayList<String>());
						}
						aug.get(count).add(result.substring(11, result.length()-12));
						firstAug = false;
						result = fileReader.nextLine().trim();
					}
					else
					{
						if (firstAug)
						{
							aug.add(new ArrayList<String>());
							aug.get(count).add("");
						}
						moreAugs = false;
					}
				}
				if (!wrongOrder && result.startsWith("<crewLimit>"))
				{
					lim.add(Integer.parseInt(result.substring(11, result.length()-12)));
				}
				else
				{
					if(!wrongOrder)
					lim.add(-1);
				}
				++count;
			}
		}
		fileReader.close();
		fle.delete();
	}
	
	private File Writer(String str) throws IOException
	{
		File file = new File("./decode");
		FileWriter writer = new FileWriter(file);
		writer.append(str);
		writer.flush();
		writer.close();
		return file;
	}
}
