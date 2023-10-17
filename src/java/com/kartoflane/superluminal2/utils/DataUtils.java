package com.kartoflane.superluminal2.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.JDOMParseException;


/**
 * This class contains utility methods used to interpret XML tags as game objects.
 * 
 */
public class DataUtils
{
	public static final Logger log = LogManager.getLogger( DataUtils.class );


	/**
	 * Finds all ships with the specified blueprintName within the file.
	 *
	 * @param fileName
	 * @param blueprintName
	 * @return
	 * @throws JDOMParseException
	 *             when the contents of the stream could not be parsed.
	 * @throws IOException
	 */
	public static ArrayList<Element> findShipsWithName( String blueprintName, InputStream is, String fileName )
		throws IllegalArgumentException, JDOMParseException, IOException
	{
		if ( blueprintName == null )
			throw new IllegalArgumentException( "Blueprint name must not be null." );

		IOUtils.DecodeResult dr = IOUtils.decodeText( is, null );
		String contents = dr.text;
		Document doc = IOUtils.parseXML( contents, true );

		ArrayList<Element> shipList = new ArrayList<Element>();

		Element root = doc.getRootElement();

		if ( root.getName().equals( "shipBlueprint" ) ) {
			String blueprint = root.getAttributeValue( "name" );

			if ( blueprint != null && blueprint.equals( blueprintName ) )
				shipList.add( root );
		}
		else {
			for ( Element e : root.getChildren( "shipBlueprint" ) ) {
				String blueprint = e.getAttributeValue( "name" );

				if ( blueprint != null && blueprint.equals( blueprintName ) )
					shipList.add( e );
			}
		}

		return shipList;
	}

	public static ArrayList<Element> findShipsWithName( File f, String blueprintName )
		throws IllegalArgumentException, JDOMParseException, IOException
	{
		return findShipsWithName( blueprintName, new FileInputStream( f ), f.getName() );
	}

	public static ArrayList<Element> findTagsNamed( String contents, String tagName ) throws JDOMParseException
	{
		Document doc = null;
		doc = IOUtils.parseXML( contents, true );
		ArrayList<Element> tagList = new ArrayList<Element>();

		Element root = doc.getRootElement();

		if ( root.getName().equals( tagName ) ) {
			tagList.add( root );
		}
		else {
			for ( Element e : root.getChildren() ) {
				if ( e.getName().equals( tagName ) ) {
					tagList.add( e );
				}
			}
		}

		return tagList;
	}

	public static ArrayList<Element> findTagsNamed( InputStream is, String fileName, String tagName )
		throws IllegalArgumentException, JDOMParseException, IOException
	{
		IOUtils.DecodeResult dr = IOUtils.decodeText( is, fileName );
		return findTagsNamed( dr.text, tagName );
	}

	public static ArrayList<Element> findTagsNamed( File f, String tagName )
		throws IllegalArgumentException, JDOMParseException, IOException
	{
		return findTagsNamed( new FileInputStream( f ), f.getName(), tagName );
	}

	/**
	 * Finds findLikes of the given type from the given text and
	 * combines them into a single element with that type as its name.
	 * These findLikes' mod-append children that match the
	 * given names are all added to this element, sans namespace.
	 * <p>
	 * Elements with mod-append namespace only appear when mods are loaded
	 * via Mod Management. Modders use this format to add their data to an
	 * original element, so there is no problem with this result.
	 * @param contents the text containing the findLike data
	 * @param findLikeType the name of the element the findLikes target
	 * @param subTagNames the names of the mod-append elements
	 * @return one element containing new data from the findLikes
	 * @throws JDOMParseException when an exception occurs while parsing XML
	 */
	public static Element transformFindLikes( String contents, String findLikeType, String... subTagNames )
			throws JDOMParseException
	{
		ArrayList<Element> findLikes = findTagsNamed( contents, "findLike" );

		ArrayList<Element> mainTagFindLikes = new ArrayList<Element>();
		for ( Element findLike : findLikes ) {
			Attribute typeAttribute = findLike.getAttribute( "type" );
			if ( typeAttribute != null && typeAttribute.getValue().equals( findLikeType ) ) {
				mainTagFindLikes.add( findLike );
			}
		}

		Element finalMainTag = new Element( findLikeType );
		for ( Element mainTagFindLike : mainTagFindLikes ) {
			Element detached = mainTagFindLike.clone();

			for ( String subTagName : subTagNames ) {
				for ( Element subTag : detached.getChildren( subTagName, SlipstreamTagNS.MOD_APPEND ) ) {
					finalMainTag.addContent( subTag.clone().setNamespace( null ) );
				}
			}
		}

		return finalMainTag;
	}
}
