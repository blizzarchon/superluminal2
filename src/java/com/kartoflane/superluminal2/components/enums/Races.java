package com.kartoflane.superluminal2.components.enums;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

final public class Races
{
	public static String[] races;
	public static String[] raceAliases;
	public static ArrayList<String> list;
	public static ArrayList<String> raceList;
	public static ArrayList<String> raceAliasList;
	
	private Races()
	{
		try 
		{
			list = new ArrayList<String>();
			raceList = new ArrayList<String>();
			raceAliasList = new ArrayList<String>();
			File raceFile = new File(System.getProperty("user.dir") + "/resources/Race list.txt");
			Scanner sc = new Scanner(raceFile);
			while (sc.hasNext())
			list.add(sc.nextLine());
			sc.close();
			
			races = new String[list.size()+1];
			raceAliases = new String[list.size()+1];
			for (int i = 0; i < list.size()+1; ++i)
			{
				raceList.add(list.get(i).split(",")[0]);
				raceAliasList.add(list.get(i).split(",")[1]);
				races[i] = list.get(i).split(",")[0];
				raceAliases[i] = list.get(i).split(",")[1];
				if (i == list.size())
				{
					raceList.add("random");
					raceAliasList.add("random");
					races[i] = "random";
					raceAliases[i] = "random";
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return an array containing all races, sans NO_CREW
	 */
	public static String[] getRaces()
	{
		try 
		{
			list = new ArrayList<String>();
			raceList = new ArrayList<String>();
			raceAliasList = new ArrayList<String>();
			File raceFile = new File(System.getProperty("user.dir") + "/resources/Race list.txt");
			Scanner sc = new Scanner(raceFile);
			while (sc.hasNext())
			list.add(sc.nextLine());
			sc.close();
			
			races = new String[list.size()+1];
			raceAliases = new String[list.size()+1];
			for (int i = 0; i < list.size()+1; ++i)
			{
				if (i != list.size())
				{
					raceList.add(list.get(i).split(",")[0]);
					raceAliasList.add(list.get(i).split(",")[1]);
					races[i] = list.get(i).split(",")[0];
					raceAliases[i] = list.get(i).split(",")[1];
				}
				if (i == list.size())
				{
					raceList.add("random");
					raceAliasList.add("random");
					races[i] = "random";
					raceAliases[i] = "random";
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return races;
	}
	public static String[] getRacesAliases()
	{
		try 
		{
			list = new ArrayList<String>();
			raceList = new ArrayList<String>();
			raceAliasList = new ArrayList<String>();
			File raceFile = new File(System.getProperty("user.dir") + "/resources/Race list.txt");
			Scanner sc = new Scanner(raceFile);
			while (sc.hasNext())
			list.add(sc.nextLine());
			sc.close();
			
			races = new String[list.size()+1];
			raceAliases = new String[list.size()+1];
			for (int i = 0; i < list.size()+1; ++i)
			{
				if (i != list.size())
				{
					raceList.add(list.get(i).split(",")[0]);
					raceAliasList.add(list.get(i).split(",")[1]);
					races[i] = list.get(i).split(",")[0];
					raceAliases[i] = list.get(i).split(",")[1];
				}
				if (i == list.size())
				{
					raceList.add("random");
					raceAliasList.add("random");
					races[i] = "random";
					raceAliases[i] = "random";
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return raceAliases;
	}

	/**
	 * @return an array containing all races, sans NO_CREW and RANDOM
	 */
	public static String[] getPlayerRaces()
	{
		try 
		{
			list = new ArrayList<String>();
			raceList = new ArrayList<String>();
			raceAliasList = new ArrayList<String>();
			File raceFile = new File(System.getProperty("user.dir") + "/resources/Race list.txt");
			Scanner sc = new Scanner(raceFile);
			while (sc.hasNext())
			list.add(sc.nextLine());
			sc.close();
			
			races = new String[list.size()+1];
			raceAliases = new String[list.size()+1];
			for (int i = 0; i < list.size()+1; ++i)
			{
				if (i != list.size())
				{
					raceList.add(list.get(i).split(",")[0]);
					raceAliasList.add(list.get(i).split(",")[1]);
					races[i] = list.get(i).split(",")[0];
					raceAliases[i] = list.get(i).split(",")[1];
				}
				if (i == list.size())
				{
					raceList.add("random");
					raceAliasList.add("random");
					races[i] = "random";
					raceAliases[i] = "random";
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list.remove("random");
		String[] returnArray;
		returnArray = raceList.toArray(new String[list.size()]);
		list.add("random");
		return returnArray;
	}
	public static String[] getPlayerRacesAliases()
	{
		try 
		{
			list = new ArrayList<String>();
			raceList = new ArrayList<String>();
			raceAliasList = new ArrayList<String>();
			File raceFile = new File(System.getProperty("user.dir") + "/resources/Race list.txt");
			Scanner sc = new Scanner(raceFile);
			while (sc.hasNext())
			list.add(sc.nextLine());
			sc.close();
			
			races = new String[list.size()+1];
			raceAliases = new String[list.size()+1];
			for (int i = 0; i < list.size()+1; ++i)
			{
				if (i != list.size())
				{
					raceList.add(list.get(i).split(",")[0]);
					raceAliasList.add(list.get(i).split(",")[1]);
					races[i] = list.get(i).split(",")[0];
					raceAliases[i] = list.get(i).split(",")[1];
				}
				if (i == list.size())
				{
					raceList.add("random");
					raceAliasList.add("random");
					races[i] = "random";
					raceAliases[i] = "random";
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		raceAliasList.remove("random");
		String[] returnArray;
		returnArray = raceAliasList.toArray(new String[list.size()]);
		list.add("random");
		return returnArray;
	}
}
