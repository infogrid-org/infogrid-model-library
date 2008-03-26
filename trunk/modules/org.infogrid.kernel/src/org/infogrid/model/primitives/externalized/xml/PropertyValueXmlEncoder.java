//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.model.primitives.externalized.xml;

import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.ColorValue;
import org.infogrid.model.primitives.EnumeratedValue;
import org.infogrid.model.primitives.ExtentValue;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MultiplicityValue;
import org.infogrid.model.primitives.PointValue;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.TimePeriodValue;
import org.infogrid.model.primitives.TimeStampValue;

import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.model.primitives.externalized.PropertyValueEncoder;

import org.infogrid.util.Base64;
import org.infogrid.util.Identifier;
import org.infogrid.util.logging.Log;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.awt.Color;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Stack;

/**
 * Utility methods to encode/decode a PropertyValue to/from XML. Implements the SAX interface.
 */
public class PropertyValueXmlEncoder
        extends
            DefaultHandler
        implements
            PropertyValueEncoder,
            PropertyValueXmlTags // import namespace
{
    private static final Log log = Log.getLogInstance( PropertyValueXmlEncoder.class ); // our own, private logger

    /**
     * Constructor.
     */
    public PropertyValueXmlEncoder()
    {
        try {
            theParser = theSaxParserFactory.newSAXParser();

        } catch( Throwable t ) {
            log.error( t );
        }
    }

    /**
     * Serialize a PropertyValue to an OutputStream.
     * 
     * @param value the PropertyValue
     * @param out the OutputStream to which to append the PropertyValue
     * @throws EncodingException thrown if a problem occurred during encoding
     * @throws IOException thrown if an I/O error occurred
     */
    public void encodePropertyValue(
            PropertyValue value,
            OutputStream  out )
        throws
            EncodingException,
            IOException
    {
        StringBuilder buf = new StringBuilder();

        appendPropertyValue( value, buf );

        OutputStreamWriter w = new OutputStreamWriter( out, ENCODING );
        w.write( buf.toString() );
        w.flush();
    }
    
    /**
     * Serialize a PropertyValue to a StringBuilder.
     * 
     * @param value the PropertyValue
     * @param buf the StringBuilder to which to append the PropertyValue
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    public void appendPropertyValue(
            PropertyValue value,
            StringBuilder buf )
        throws
            EncodingException
    {        
        if( value == null ) {
            buf.append( "<" ).append( NULL_VALUE ).append( "/>" );

        } else if( value instanceof BlobValue ) {
            BlobValue realValue = (BlobValue) value;
            String    mt        = realValue.getMimeType();
            if( mt == null ) {
                mt = "?/?";
            }

            buf.append( "<" ).append( BLOB_VALUE_TAG );
            buf.append( " " ).append( BLOB_VALUE_MIME_TAG ).append( "=\"" ).append( mt ).append( "\"" );

            if( realValue.delayedLoadingFrom() != null ) {
                buf.append( " " ).append( BLOB_VALUE_LOAD_TAG ).append( "=\"" ).append( escape( realValue.delayedLoadingFrom() )).append( "\"/>" );

            } else {
                buf.append( ">" );
                if( mt.startsWith( "text/" )) {
                    buf.append( "<![CDATA[" );
                    buf.append( cdataEscape( realValue.getAsString() ) );
                    buf.append( "]]>" );
                } else {
                    buf.append( Base64.base64encode( realValue.value() ) );
                }
                buf.append( "</" ).append( BLOB_VALUE_TAG ).append( ">" );
            }

        } else if( value instanceof BooleanValue ) {
            buf.append( "<" ).append( BOOLEAN_VALUE_TAG ).append( ">" );
            if( ((BooleanValue)value).value() ) {
                buf.append( BOOLEAN_VALUE_TRUE_TAG );
            } else {
                buf.append( BOOLEAN_VALUE_FALSE_TAG );
            }
            buf.append( "</" ).append( BOOLEAN_VALUE_TAG ).append( ">" );

        } else if( value instanceof ColorValue ) {
            Color theColor = ((ColorValue)value).value();
            buf.append( "<" ).append( COLOR_VALUE_TAG ).append( ">" );
            buf.append( String.valueOf( theColor.getRGB()) );
            buf.append( "</" ).append( COLOR_VALUE_TAG ).append( ">" );

        } else if( value instanceof EnumeratedValue ) {
            EnumeratedValue realValue = (EnumeratedValue) value;
            buf.append( "<" ).append( ENUMERATED_VALUE_TAG ).append( ">" );
            buf.append( escape( realValue.value() ));
            buf.append( "</" ).append( ENUMERATED_VALUE_TAG ).append( ">" );

        } else if( value instanceof ExtentValue ) {
            ExtentValue realValue = (ExtentValue) value;
            buf.append( "<" ).append( EXTENT_VALUE_TAG );
            buf.append( " " ).append( EXTENT_VALUE_WIDTH_TAG ).append( "=\"" ).append( realValue.getWidth() ).append( "\"" );
            buf.append( " " ).append( EXTENT_VALUE_HEIGHT_TAG ).append( "=\"" ).append( realValue.getHeight() ).append( "\"/>" );

        } else if( value instanceof FloatValue ) {
            FloatValue realValue = (FloatValue) value;
            buf.append( "<" ).append( FLOAT_VALUE_TAG ).append( ">" );
            buf.append( realValue.value() );
            buf.append( "</" ).append( FLOAT_VALUE_TAG ).append( ">" );

        } else if( value instanceof IntegerValue ) {
            IntegerValue realValue = (IntegerValue) value;
            buf.append( "<" ).append( INTEGER_VALUE_TAG ).append( ">" );
            buf.append( realValue.value() );
            buf.append( "</" ).append( INTEGER_VALUE_TAG ).append( ">" );

        } else if( value instanceof MultiplicityValue ) {
            MultiplicityValue realValue = (MultiplicityValue) value;
            buf.append( "<" ).append( MULTIPLICITY_VALUE_TAG );
            if( realValue.getMinimum() >= 0 ) {
                buf.append( " " ).append( MULTIPLICITY_VALUE_MIN_TAG ).append( "=\"" ).append( realValue.getMinimum() ).append( "\"" );
            }
            if( realValue.getMaximum() >= 0 ) {
                buf.append( " " ).append( MULTIPLICITY_VALUE_MAX_TAG ).append( "=\"" ).append( realValue.getMaximum() ).append( "\"" );
            }
            buf.append( "/>" );

        } else if( value instanceof PointValue ) {
            PointValue realValue = (PointValue) value;
            buf.append( "<" ).append( POINT_VALUE_TAG );
            buf.append( " " ).append( POINT_VALUE_X_TAG ).append( "=\"" ).append( realValue.getX() ).append( "\"" );
            buf.append( " " ).append( POINT_VALUE_Y_TAG ).append( "=\"" ).append( realValue.getY() ).append( "\"/>" );

        } else if( value instanceof StringValue ) {
            StringValue realValue = (StringValue) value;
            buf.append( "<" ).append( STRING_VALUE_TAG ).append( ">" );
            buf.append( escape( realValue.value() ));
            buf.append( "</" ).append( STRING_VALUE_TAG ).append( ">" );

        } else if( value instanceof TimePeriodValue ) {
            TimePeriodValue realValue = (TimePeriodValue) value;
            buf.append( "<" ).append( TIME_PERIOD_TAG );
            buf.append( " " ).append( TIME_PERIOD_YEAR_TAG   ).append( "=\"" ).append( realValue.getYear() ).append( "\"" );
            buf.append( " " ).append( TIME_PERIOD_MONTH_TAG  ).append( "=\"" ).append( realValue.getMonth() ).append( "\"" );
            buf.append( " " ).append( TIME_PERIOD_DAY_TAG    ).append( "=\"" ).append( realValue.getDay() ).append( "\"" );
            buf.append( " " ).append( TIME_PERIOD_HOUR_TAG   ).append( "=\"" ).append( realValue.getHour() ).append( "\"" );
            buf.append( " " ).append( TIME_PERIOD_MINUTE_TAG ).append( "=\"" ).append( realValue.getMinute() ).append( "\"" );
            buf.append( " " ).append( TIME_PERIOD_SECOND_TAG ).append( "=\"" ).append( realValue.getSecond() ).append( "\"/>" );

        } else if( value instanceof TimeStampValue ) {
            TimeStampValue realValue = (TimeStampValue) value;
            buf.append( "<" ).append( TIME_STAMP_TAG );
            buf.append( " " ).append( TIME_STAMP_YEAR_TAG   ).append( "=\"" ).append( realValue.getYear() ).append( "\"" );
            buf.append( " " ).append( TIME_STAMP_MONTH_TAG  ).append( "=\"" ).append( realValue.getMonth() ).append( "\"" );
            buf.append( " " ).append( TIME_STAMP_DAY_TAG    ).append( "=\"" ).append( realValue.getDay() ).append( "\"" );
            buf.append( " " ).append( TIME_STAMP_HOUR_TAG   ).append( "=\"" ).append( realValue.getHour() ).append( "\"" );
            buf.append( " " ).append( TIME_STAMP_MINUTE_TAG ).append( "=\"" ).append( realValue.getMinute() ).append( "\"" );
            buf.append( " " ).append( TIME_STAMP_SECOND_TAG ).append( "=\"" ).append( realValue.getSecond() ).append( "\"/>" );

        } else {
            buf.append( "?" );
        }
    }

    /**
     * Deserialize a PropertyValue from a byte stream.
     * 
     * @param contentAsStream the byte [] stream in which the PropertyValue is encoded
     * @return return the just-instantiated PropertyValue
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    public PropertyValue decodePropertyValue(
            InputStream contentAsStream )
        throws
            DecodingException,
            IOException
    {
        try {
            theParser.parse( contentAsStream, this );
            return thePropertyValue;

        } catch( SAXException ex ) {
            throw new DecodingException( ex );

        } finally {
            clearState();
        }
    }

    /**
     * Obtain an encodingId that reflects this ExternalizedMeshObjectEncoder.
     * 
     * @return the encodingId.
     */
    public String getEncodingId()
    {
        return getClass().getName();
    }

    /**
     * Override locator method so we know what we are parsing.
     *
     * @param locator the new Locator object
     */
    @Override
    public void setDocumentLocator(
            Locator locator )
    {
        theLocator = locator;
    }

    /**
     * SAX found some characters.
     *
     * @param ch the character array
     * @param start the start index
     * @param length the number of characters
     */
    @Override
    public final void characters(
            char [] ch,
            int     start,
            int     length )
    {
        try {
            if( length > 0 ) {
                if( theCharacters == null ) {
                    theCharacters = new StringBuffer();
                }
                theCharacters.append( ch, start, length );
            }
        } catch( RuntimeException ex ) {
            log.error( ex ); // internal error, no need to throw SAXParseException
        }
    }

    /**
     * SAX says a new element starts.
     *
     * @param namespaceURI URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     */
    @Override
    public void startElement(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        if( log.isInfoEnabled() ) {
            log.info( "SAX startElement: " + namespaceURI + ", " + localName + ", " + qName );
        }
        theCharacters = null; // if we haven't processed them so far, we never will

        if( NULL_VALUE.equals( qName )) {
            // no op
        } else if( BLOB_VALUE_TAG.equals( qName )) {
            String mime      = attrs.getValue( BLOB_VALUE_MIME_TAG );
            String loadFrom  = attrs.getValue( BLOB_VALUE_LOAD_TAG );

            if( mime != null && mime.length() > 0 ) {
                BlobValue propertyValue;
                if( loadFrom != null && loadFrom.length() > 0 ) {
                    propertyValue = BlobValue.createByLoadingFrom( loadFrom, mime );
                } else {
                    propertyValue = BlobValue.create( "placeholder", mime ); // that's a big ugly, but why not
                }
                thePropertyValue = propertyValue;
            } else {
                log.error( "empty '" + BLOB_VALUE_MIME_TAG + "' on '" + BLOB_VALUE_TAG + "'" );
            }
            
        } else if( BOOLEAN_VALUE_TAG.equals( qName )) {
            // no op
        } else if( COLOR_VALUE_TAG.equals( qName )) {
            // no op
        } else if( ENUMERATED_VALUE_TAG.equals( qName )) {
            // no op
        } else if( EXTENT_VALUE_TAG.equals( qName )) {
            String width  = attrs.getValue( EXTENT_VALUE_WIDTH_TAG );
            String height = attrs.getValue( EXTENT_VALUE_HEIGHT_TAG );

            if( width == null || width.length() == 0 ) {
                log.error( "empty '" + EXTENT_VALUE_WIDTH_TAG + "' on '" + EXTENT_VALUE_TAG + "'" );
            }            
            if( height == null && height.length() == 0 ) {
                log.error( "empty '" + EXTENT_VALUE_HEIGHT_TAG + "' on '" + EXTENT_VALUE_TAG + "'" );
            }
            thePropertyValue = ExtentValue.create( Double.parseDouble( width ), Double.parseDouble( height ));

        } else if( INTEGER_VALUE_TAG.equals( qName )) {
            // no op
        } else if( FLOAT_VALUE_TAG.equals( qName )) {
            // no op
        } else if( MULTIPLICITY_VALUE_TAG.equals( qName )) {
            String min = attrs.getValue( MULTIPLICITY_VALUE_MIN_TAG );
            String max = attrs.getValue( MULTIPLICITY_VALUE_MAX_TAG );

            thePropertyValue = MultiplicityValue.create(
                    ( min != null && min.length() > 0 ) ? Integer.parseInt( min ) : MultiplicityValue.N,
                    ( max != null && max.length() > 0 ) ? Integer.parseInt( max ) : MultiplicityValue.N );

        } else if( POINT_VALUE_TAG.equals( qName )) {
            String x = attrs.getValue( POINT_VALUE_X_TAG );
            String y = attrs.getValue( POINT_VALUE_Y_TAG );

            if( x == null || x.length() == 0 ) {
                log.error( "empty '" + POINT_VALUE_X_TAG + "' on '" + POINT_VALUE_TAG + "'" );
            }
            if( y == null || y.length() == 0 ) {
                log.error( "empty '" + POINT_VALUE_Y_TAG + "' on '" + POINT_VALUE_TAG + "'" );
            }
            thePropertyValue = PointValue.create( Double.parseDouble( x ), Double.parseDouble( y ));
                    
        } else if( STRING_VALUE_TAG.equals( qName )) {
            // no op
        } else if( TIME_PERIOD_TAG.equals( qName )) {
            String yr  = attrs.getValue( TIME_PERIOD_YEAR_TAG );
            String mon = attrs.getValue( TIME_PERIOD_MONTH_TAG );
            String day = attrs.getValue( TIME_PERIOD_DAY_TAG );
            String hr  = attrs.getValue( TIME_PERIOD_HOUR_TAG );
            String min = attrs.getValue( TIME_PERIOD_MINUTE_TAG );
            String sec = attrs.getValue( TIME_PERIOD_SECOND_TAG );

            if( yr == null || yr.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_YEAR_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( mon == null || mon.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_MONTH_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( day == null || day.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_DAY_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( hr == null || hr.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_HOUR_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( min == null || min.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_MINUTE_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( sec == null || sec.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_SECOND_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            
            thePropertyValue = TimePeriodValue.create(
                    Short.parseShort( yr ),
                    Short.parseShort( mon ),
                    Short.parseShort( day ),
                    Short.parseShort( hr ),
                    Short.parseShort( min ),
                    Float.parseFloat( sec ));

        } else if( TIME_STAMP_TAG.equals( qName )) {
            String yr  = attrs.getValue( TIME_STAMP_YEAR_TAG );
            String mon = attrs.getValue( TIME_STAMP_MONTH_TAG );
            String day = attrs.getValue( TIME_STAMP_DAY_TAG );
            String hr  = attrs.getValue( TIME_STAMP_HOUR_TAG );
            String min = attrs.getValue( TIME_STAMP_MINUTE_TAG );
            String sec = attrs.getValue( TIME_STAMP_SECOND_TAG );

            if( yr == null || yr.length() == 0 ) {
                log.error( "empty '" + TIME_STAMP_YEAR_TAG + "' on '" + TIME_STAMP_TAG + "'" );
            }
            if( mon == null || mon.length() == 0 ) {
                log.error( "empty '" + TIME_STAMP_MONTH_TAG + "' on '" + TIME_STAMP_TAG + "'" );
            }
            if( day == null || day.length() == 0 ) {
                log.error( "empty '" + TIME_STAMP_DAY_TAG + "' on '" + TIME_STAMP_TAG + "'" );
            }
            if( hr == null || hr.length() == 0 ) {
                log.error( "empty '" + TIME_STAMP_HOUR_TAG + "' on '" + TIME_STAMP_TAG + "'" );
            }
            if( min == null || min.length() == 0 ) {
                log.error( "empty '" + TIME_STAMP_MINUTE_TAG + "' on '" + TIME_STAMP_TAG + "'" );
            }
            if( sec == null || sec.length() == 0 ) {
                log.error( "empty '" + TIME_STAMP_SECOND_TAG + "' on '" + TIME_STAMP_TAG + "'" );
            }
            
            thePropertyValue = TimeStampValue.create(
                    Short.parseShort( yr ),
                    Short.parseShort( mon ),
                    Short.parseShort( day ),
                    Short.parseShort( hr ),
                    Short.parseShort( min ),
                    Float.parseFloat( sec ));

        } else {
            startElement1( namespaceURI, localName, qName, attrs );
        }
    }

    /**
     * Allows subclasses to add to parsing.
     *
     * @param namespaceURI URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     */
    protected void startElement1(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        log.error( "unknown qname " + qName );
    }

    /**
     * SAX says an element ends.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     */
    @Override
    public void endElement(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( log.isInfoEnabled() ) {
            log.info( "SAX endElement: " + namespaceURI + ", " + localName + ", " + qName );
        }

        if( NULL_VALUE.equals( qName )) {
            thePropertyValue = null;

        } else if( BLOB_VALUE_TAG.equals( qName )) {
            if( ( thePropertyValue instanceof BlobValue ) && ((BlobValue)thePropertyValue).delayedLoadingFrom() == null ) {
                if( ((BlobValue)thePropertyValue).getMimeType().startsWith( "text/" ) ) {
                    thePropertyValue = BlobValue.create(
                            theCharacters != null ? cdataDescape( theCharacters.toString().trim()) : "",
                            ((BlobValue)thePropertyValue).getMimeType() );
                } else {
                    thePropertyValue = BlobValue.create( Base64.base64decode( theCharacters.toString().trim() ), ((BlobValue)thePropertyValue).getMimeType() );
                }
            }
            
        } else if( BOOLEAN_VALUE_TAG.equals( qName )) {
            if( theCharacters != null ) {
                thePropertyValue = BooleanValue.create( BOOLEAN_VALUE_TRUE_TAG.equals( theCharacters.toString().trim() ));
            } else {
                throw new SAXException( "No value given for BooleanValue" );
            }

        } else if( COLOR_VALUE_TAG.equals( qName )) {
            if( theCharacters != null ) {
                thePropertyValue = ColorValue.parseColorValue( theCharacters.toString() );
            } else {
                throw new SAXException( "No value given for ColorValue" );
            }
            
        } else if( ENUMERATED_VALUE_TAG.equals( qName )) {
            if( theCharacters != null ) {
                thePropertyValue = EnumeratedValue.create( null, theCharacters.toString().trim(), null, null );
                    // This needs to be patched later once we have the instance of EnumeratedDataType
            } else {
                throw new SAXException( "No value given for EnumeratedValue" );
            }

        } else if( EXTENT_VALUE_TAG.equals( qName )) {
            // no op

        } else if( INTEGER_VALUE_TAG.equals( qName )) {
            if( theCharacters != null ) {
                thePropertyValue = IntegerValue.parseIntegerValue( theCharacters.toString() );
            } else {
                throw new SAXException( "No value given for IntegerValue" );
            }

        } else if( FLOAT_VALUE_TAG.equals( qName )) {
            if( theCharacters != null ) {
                thePropertyValue = FloatValue.parseFloatValue( theCharacters.toString() );
            } else {
                throw new SAXException( "No value given for FloatValue" );
            }

        } else if( MULTIPLICITY_VALUE_TAG.equals( qName )) {
            // no op

        } else if( POINT_VALUE_TAG.equals( qName )) {
            // no op

        } else if( STRING_VALUE_TAG.equals( qName )) {
            thePropertyValue = StringValue.create(
                    theCharacters != null ? theCharacters.toString() : "" ); // let's not strip() here

        } else if( TIME_PERIOD_TAG.equals( qName )) {
            // no op

        } else if( TIME_STAMP_TAG.equals( qName )) {
            // no op

        } else {
            endElement1( namespaceURI, localName, qName );
        }
    }

    /**
     * Allows subclasses to add to parsing.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     */
    protected void endElement1(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        log.error( "unknown qname " + qName );
    }
    
    /**
     * Reset the parser.
     */
    public void clearState()
    {
        theCharacters    = null;
        thePropertyValue = null;

        theStack.clear();
    }

    /**
     * Throw exception in case of an Exception indicating an error.
     *
     * @param ex the Exception
     * @throws SAXParseException the passed-in Exception
     */
    public final void error(
            Exception ex )
        throws
            SAXParseException
    {
        if( ex instanceof SAXParseException ) {
            throw ((SAXParseException)ex);
        } else {
            throw new FixedSAXParseException( theErrorPrefix, theLocator, ex );
        }
    }


    /**
     * Encode a long value.
     *
     * @param value the long value to encode
     * @param buf the StringBuilder to append to
     */
    public static void appendLong(
            long          value,
            StringBuilder buf )
    {
        buf.append( value );
    }

    /**
     * Encoding an Identifier without enclosing it in <tag></tag> tags.
     * 
     * @param id the Identifier
     * @param buf the StringBuffer to append to
     */
    protected void appendIdentifier(
            Identifier     id,
            StringBuilder  buf )
    {
        buf.append( escape( id.toExternalForm()));
    }

    /**
     * Parse an XML DOM attribute into a long, or use default.
     *
     * @param attrs the Attributes on the XML DOM node
     * @param attName name of the Attribute
     * @param defaultValue
     * @return the value
     */
    protected static long parseLong(
            Attributes attrs,
            String     attName,
            long       defaultValue )
    {
        String attValue = attrs.getValue( attName );
        return parseLong( attValue, defaultValue );
    }

    /**
     * Parse an String into a long, or use default.
     *
     * @param s the String
     * @param defaultValue
     * @return the value
     */
    protected static long parseLong(
            String     s,
            long       defaultValue )
    {
        if( s == null || s.length() == 0 ) {
            return defaultValue;
        }
        long ret = Long.parseLong( s );
        return ret;
    }

    /**
     * Parse an XML DOM attribute into an int, or use default.
     *
     * @param attrs the Attributes on the XML DOM node
     * @param attName name of the Attribute
     * @param defaultValue
     * @return the value
     */
    protected static int parseInt(
            Attributes attrs,
            String     attName,
            int        defaultValue )
    {
        String attValue = attrs.getValue( attName );
        return parseInt( attValue, defaultValue );
    }

    /**
     * Parse an String into a int, or use default.
     *
     * @param s the String
     * @param defaultValue
     * @return the value
     */
    protected static int parseInt(
            String     s,
            int        defaultValue )
    {
        if( s == null || s.length() == 0 ) {
            return defaultValue;
        }
        int ret = Integer.parseInt( s );
        return ret;
    }

    /**
     * This helper takes arbitrary strings and escapes them in a way so that they
     * fit into XML. FIXME This is not complete at all.
     *
     * @param org the original String
     * @return the escaped String
     * @see #descape
     */
    public static final String escape(
            String org )
    {
        StringBuffer ret = new StringBuffer( org.length() + org.length()/5 ); // make a little larger
        for( int i=0 ; i<org.length() ; ++i ) {
            char c = org.charAt( i );
            boolean escape = false;

            switch( c ) {
                case '\'':
                case '\"':
                case '\t':
                // case '\n':
                case '\\':
                case '&':
                // case '#':
                case '<':
                case '>':
                // case '/':
                    escape = true;
                    break;
                default:
                    if( c < 0x20 ) {
                        escape = true;
                    }
                    break;
            }
            if( escape ) {
                ret.append( "&#x" );
                // needs to have a least four hex digits according to the XML spec
                int digit;
                for( int j=3 ; j>=0 ; --j ) {
                    digit = c >> (j*4);
                    digit &= 0xf;
                    ret.append( charTable[ digit ] );
                }
                ret.append( ";" );
            } else {
                ret.append( c );
                
            }
        }
        return new String( ret );
    }


    /**
     * This helper takes a String escaped according to escape and undos the escape.
     * FIXME This is not complete at all.
     *
     * @param enc the escaped String
     * @return the original String
     * @see #escape
     */
    public static final String descape(
            String enc )
    {
        StringBuffer ret = new StringBuffer( enc.length() ); // same length

        for( int i=0 ; i<enc.length() ; ++i ) {
            char c = enc.charAt( i );
            switch( c ) {
                case '&':
                    if( enc.charAt( i+1 ) == '#' ) {
                        i += 3; // we simply believe that the next two are going to be #x. FIXME?
                        String s = enc.substring( i );
                        int colonIndex = s.indexOf( ';' );
                        int v = Integer.parseInt( s.substring( 0, colonIndex ), 16 );

                        ret.append( new Character( (char) v ));

                        i += colonIndex;
                        break;
                    }
                    // else run-through

                default:
                    ret.append( c );
                    break;
            }
        }
        return ret.toString();
    }
    
    /**
     * This helper takes escaped strings and undoes what cdataEscape did.
     *
     * @param enc the escaped String
     * @return the original String
     * @see #cdataEscape
     */
    public static final String cdataDescape(
            String enc )
    {
        return descape( enc );
        // FIXME: This should do CDATA, because it is more efficient, but apparently we lose 0x1-0x1f in the process or something like that
//        final String escaped  = "&#93;&#93;&gt;"; // Exact char sequence required, see cdataEscape
//        final String toEscape = "]]>";
//
//        StringBuffer ret = new StringBuffer( enc.length()); // no need to make longer, needing to escape is very unusual
//        int start = 0;
//        int end;
//        while( ( end = enc.indexOf( escaped, start )) >= 0 ) {
//            ret.append( enc.substring( start, end ));
//            ret.append( toEscape );
//            start = end + escaped.length();
//        }
//        if( start < enc.length() ) {
//            ret.append( enc.substring( start ));
//        }
//        return ret.toString();
    }
    
    
    /**
     * This helper takes arbitrary strings and escapes them in a way so that they
     * fit into XML CDATA sections.
     *
     * @param org the original String
     * @return the escaped String
     * @see #cdataDescape
     */
    public static final String cdataEscape(
            String org )
    {
        return escape( org );
        // FIXME: This should do CDATA, because it is more efficient, but apparently we lose 0x1-0x1f in the process or something like that
//        final String toEscape = "]]>";
//        final String escaped  = "&#93;&#93;&gt;"; // Exact char sequence required, see cdataDescape
//        
//        StringBuffer ret = new StringBuffer( org.length()); // no need to make longer, needing to escape is very unusual
//        int start = 0;
//        int end;
//        while( ( end = org.indexOf( toEscape, start )) >= 0 ) {
//            ret.append( org.substring( start, end ));
//            ret.append( escaped );
//            start = end + toEscape.length();
//        }
//        if( start < org.length() ) {
//            ret.append( org.substring( start ));
//        }
//        return ret.toString();
    }

    /**
     * For conversion speed, a character table.
     */
    protected static char [] charTable = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * The SAX parser factory to use.
     */
    protected static final SAXParserFactory theSaxParserFactory;
    static {
        theSaxParserFactory = SAXParserFactory.newInstance();
        theSaxParserFactory.setValidating( true );
    }

    /**
     * The character encoding that we are using.
     */
    public static final String ENCODING = "UTF-8";

    /**
     * Our SAX parser.
     */
    protected SAXParser theParser;
    
    /**
     * The error message prefix in case we need it.
     */
    protected String theErrorPrefix;

    /**
     * The Locator object that tells us where we are in the parsed file.
     */
    protected Locator theLocator;

    /**
     * The parse stack.
     */
    protected Stack<Object> theStack = new Stack<Object>();

    /**
     * The character String that is currently being parsed, if any.
     */
    protected StringBuffer theCharacters = null;

    /**
     * The PropertyValue that was successfully parsed.
     */
    protected PropertyValue thePropertyValue = null;

    /**
     * Java FixedSAXParseException's constructor is broken, so we created this workaround class.
     */
    static class FixedSAXParseException
            extends
                SAXParseException
    {
        public FixedSAXParseException(
                String    message,
                Locator   locator,
                Exception cause )
        {
            super( message, locator, cause );
            
            initCause( cause );
        }
    }
}
