/*
 * java.util.Properties.java modified by Kevin Gaudin to allow usage of enums as keys.
 * 
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.acra;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Stores a crash reports data with {@link ReportField} enum values as keys.
 * This is basically the source of {@link Properties} adapted to extend an
 * EnumMap instead of Hashtable and with a few tweaks to avoid losing crazy
 * amounts of android time in the generation of a date comment when storing to
 * file.
 */
public class CrashReportData extends EnumMap<ReportField, String> {

    private static final long serialVersionUID = 4112578634029874840L;

    // private transient DocumentBuilder builder = null;

    private static final String PROP_DTD_NAME = "http://java.sun.com/dtd/properties.dtd";

    // private static final String PROP_DTD =
    // "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    // + "    <!ELEMENT properties (comment?, entry*) >"
    // + "    <!ATTLIST properties version CDATA #FIXED \"1.0\" >"
    // + "    <!ELEMENT comment (#PCDATA) >"
    // + "    <!ELEMENT entry (#PCDATA) >"
    // + "    <!ATTLIST entry key CDATA #REQUIRED >";

    /**
     * The default values for keys not found in this {@code Properties}
     * instance.
     */
    protected CrashReportData defaults;

    private static final int NONE = 0, SLASH = 1, UNICODE = 2, CONTINUE = 3, KEY_DONE = 4, IGNORE = 5;

    /**
     * Constructs a new {@code Properties} object.
     */
    public CrashReportData() {
        super(ReportField.class);
    }

    /**
     * Constructs a new {@code Properties} object using the specified default
     * {@code Properties}.
     * 
     * @param properties
     *            the default {@code Properties}.
     */
    public CrashReportData(CrashReportData properties) {
        super(ReportField.class);
        defaults = properties;
    }

    private void dumpString(StringBuilder buffer, String string, boolean key) {
        int i = 0;
        if (!key && i < string.length() && string.charAt(i) == ' ') {
            buffer.append("\\ "); //$NON-NLS-1$
            i++;
        }

        for (; i < string.length(); i++) {
            char ch = string.charAt(i);
            switch (ch) {
            case '\t':
                buffer.append("\\t"); //$NON-NLS-1$
                break;
            case '\n':
                buffer.append("\\n"); //$NON-NLS-1$
                break;
            case '\f':
                buffer.append("\\f"); //$NON-NLS-1$
                break;
            case '\r':
                buffer.append("\\r"); //$NON-NLS-1$
                break;
            default:
                if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' ')) {
                    buffer.append('\\');
                }
                if (ch >= ' ' && ch <= '~') {
                    buffer.append(ch);
                } else {
                    String hex = Integer.toHexString(ch);
                    buffer.append("\\u"); //$NON-NLS-1$
                    for (int j = 0; j < 4 - hex.length(); j++) {
                        buffer.append("0"); //$NON-NLS-1$
                    }
                    buffer.append(hex);
                }
            }
        }
    }

    /**
     * Searches for the property with the specified name. If the property is not
     * found, the default {@code Properties} are checked. If the property is not
     * found in the default {@code Properties}, {@code null} is returned.
     * 
     * @param key
     *            the name of the property to find.
     * @return the named property value, or {@code null} if it can't be found.
     */
    public String getProperty(ReportField key) {
        String result = super.get(key);
        if (result == null && defaults != null) {
            result = defaults.getProperty(key);
        }
        return result;
    }

    /**
     * Searches for the property with the specified name. If the property is not
     * found, it looks in the default {@code Properties}. If the property is not
     * found in the default {@code Properties}, it returns the specified
     * default.
     * 
     * @param key
     *            the name of the property to find.
     * @param defaultValue
     *            the default value.
     * @return the named property value.
     */
    public String getProperty(ReportField key, String defaultValue) {
        String property = super.get(key);
        if (property == null && defaults != null) {
            property = defaults.getProperty(key);
        }
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

    /**
     * Lists the mappings in this {@code Properties} to the specified
     * {@code PrintStream} in a human readable form.
     * 
     * @param out
     *            the {@code PrintStream} to write the content to in human
     *            readable form.
     */
    public void list(PrintStream out) {
        if (out == null) {
            throw new NullPointerException();
        }
        StringBuilder buffer = new StringBuilder(80);
        Enumeration<ReportField> keys = keys();
        while (keys.hasMoreElements()) {
            ReportField key = keys.nextElement();
            buffer.append(key);
            buffer.append('=');
            String property = (String) super.get(key);
            CrashReportData def = defaults;
            while (property == null) {
                property = (String) def.get(key);
                def = def.defaults;
            }
            if (property.length() > 40) {
                buffer.append(property.substring(0, 37));
                buffer.append("..."); //$NON-NLS-1$
            } else {
                buffer.append(property);
            }
            out.println(buffer.toString());
            buffer.setLength(0);
        }
    }

    /**
     * Lists the mappings in this {@code Properties} to the specified
     * {@code PrintWriter} in a human readable form.
     * 
     * @param writer
     *            the {@code PrintWriter} to write the content to in human
     *            readable form.
     */
    public void list(PrintWriter writer) {
        if (writer == null) {
            throw new NullPointerException();
        }
        StringBuilder buffer = new StringBuilder(80);
        Enumeration<ReportField> keys = keys();
        while (keys.hasMoreElements()) {
            ReportField key = keys.nextElement();
            buffer.append(key);
            buffer.append('=');
            String property = (String) super.get(key);
            CrashReportData def = defaults;
            while (property == null) {
                property = (String) def.get(key);
                def = def.defaults;
            }
            if (property.length() > 40) {
                buffer.append(property.substring(0, 37));
                buffer.append("..."); //$NON-NLS-1$
            } else {
                buffer.append(property);
            }
            writer.println(buffer.toString());
            buffer.setLength(0);
        }
    }

    /**
     * Loads properties from the specified {@code InputStream}. The encoding is
     * ISO8859-1.
     * 
     * @param in
     *            the {@code InputStream}.
     * @throws IOException
     *             if error occurs during reading from the {@code InputStream}.
     */
    public synchronized void load(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException();
        }
        BufferedInputStream bis = new BufferedInputStream(in);
        bis.mark(Integer.MAX_VALUE);
        boolean isEbcdic = isEbcdic(bis);
        bis.reset();

        if (!isEbcdic) {
            load(new InputStreamReader(bis, "ISO8859-1")); //$NON-NLS-1$
        } else {
            load(new InputStreamReader(bis)); //$NON-NLS-1$
        }
    }

    private boolean isEbcdic(BufferedInputStream in) throws IOException {
        byte b;
        while ((b = (byte) in.read()) != -1) {
            if (b == 0x23 || b == 0x0a || b == 0x3d) {// ascii: newline/#/=
                return false;
            }
            if (b == 0x15) {// EBCDIC newline
                return true;
            }
        }
        // we found no ascii newline, '#', neither '=', relative safe to
        // consider it
        // as non-ascii, the only exception will be a single line with only
        // key(no value and '=')
        // in this case, it should be no harm to read it in default charset
        return false;
    }

    /**
     * Loads properties from the specified InputStream. The properties are of
     * the form <code>key=value</code>, one property per line. It may be not
     * encode as 'ISO-8859-1'.The {@code Properties} file is interpreted
     * according to the following rules:
     * <ul>
     * <li>Empty lines are ignored.</li>
     * <li>Lines starting with either a "#" or a "!" are comment lines and are
     * ignored.</li>
     * <li>A backslash at the end of the line escapes the following newline
     * character ("\r", "\n", "\r\n"). If there's a whitespace after the
     * backslash it will just escape that whitespace instead of concatenating
     * the lines. This does not apply to comment lines.</li>
     * <li>A property line consists of the key, the space between the key and
     * the value, and the value. The key goes up to the first whitespace, "=" or
     * ":" that is not escaped. The space between the key and the value contains
     * either one whitespace, one "=" or one ":" and any number of additional
     * whitespaces before and after that character. The value starts with the
     * first character after the space between the key and the value.</li>
     * <li>Following escape sequences are recognized: "\ ", "\\", "\r", "\n",
     * "\!", "\#", "\t", "\b", "\f", and "&#92;uXXXX" (unicode character).</li>
     * </ul>
     * 
     * @param reader
     *            the input reader
     * @throws IOException
     * @since 1.6
     */
    public synchronized void load(Reader reader) throws IOException {
        int mode = NONE, unicode = 0, count = 0;
        char nextChar, buf[] = new char[40];
        int offset = 0, keyLength = -1, intVal;
        boolean firstChar = true;
        BufferedReader br = new BufferedReader(reader);

        while (true) {
            intVal = br.read();
            if (intVal == -1)
                break;
            nextChar = (char) intVal;

            if (offset == buf.length) {
                char[] newBuf = new char[buf.length * 2];
                System.arraycopy(buf, 0, newBuf, 0, offset);
                buf = newBuf;
            }
            if (mode == UNICODE) {
                int digit = Character.digit(nextChar, 16);
                if (digit >= 0) {
                    unicode = (unicode << 4) + digit;
                    if (++count < 4) {
                        continue;
                    }
                } else if (count <= 4) {
                    // luni.09=Invalid Unicode sequence: illegal character
                    throw new IllegalArgumentException("luni.09");
                }
                mode = NONE;
                buf[offset++] = (char) unicode;
                if (nextChar != '\n' && nextChar != '\u0085') {
                    continue;
                }
            }
            if (mode == SLASH) {
                mode = NONE;
                switch (nextChar) {
                case '\r':
                    mode = CONTINUE; // Look for a following \n
                    continue;
                case '\u0085':
                case '\n':
                    mode = IGNORE; // Ignore whitespace on the next line
                    continue;
                case 'b':
                    nextChar = '\b';
                    break;
                case 'f':
                    nextChar = '\f';
                    break;
                case 'n':
                    nextChar = '\n';
                    break;
                case 'r':
                    nextChar = '\r';
                    break;
                case 't':
                    nextChar = '\t';
                    break;
                case 'u':
                    mode = UNICODE;
                    unicode = count = 0;
                    continue;
                }
            } else {
                switch (nextChar) {
                case '#':
                case '!':
                    if (firstChar) {
                        while (true) {
                            intVal = br.read();
                            if (intVal == -1)
                                break;
                            nextChar = (char) intVal; // & 0xff
                                                      // not
                                                      // required
                            if (nextChar == '\r' || nextChar == '\n' || nextChar == '\u0085') {
                                break;
                            }
                        }
                        continue;
                    }
                    break;
                case '\n':
                    if (mode == CONTINUE) { // Part of a \r\n sequence
                        mode = IGNORE; // Ignore whitespace on the next line
                        continue;
                    }
                    // fall into the next case
                case '\u0085':
                case '\r':
                    mode = NONE;
                    firstChar = true;
                    if (offset > 0 || (offset == 0 && keyLength == 0)) {
                        if (keyLength == -1) {
                            keyLength = offset;
                        }
                        String temp = new String(buf, 0, offset);
                        put(Enum.valueOf(ReportField.class, temp.substring(0, keyLength)), temp.substring(keyLength));
                    }
                    keyLength = -1;
                    offset = 0;
                    continue;
                case '\\':
                    if (mode == KEY_DONE) {
                        keyLength = offset;
                    }
                    mode = SLASH;
                    continue;
                case ':':
                case '=':
                    if (keyLength == -1) { // if parsing the key
                        mode = NONE;
                        keyLength = offset;
                        continue;
                    }
                    break;
                }
                if (Character.isWhitespace(nextChar)) {
                    if (mode == CONTINUE) {
                        mode = IGNORE;
                    }
                    // if key length == 0 or value length == 0
                    if (offset == 0 || offset == keyLength || mode == IGNORE) {
                        continue;
                    }
                    if (keyLength == -1) { // if parsing the key
                        mode = KEY_DONE;
                        continue;
                    }
                }
                if (mode == IGNORE || mode == CONTINUE) {
                    mode = NONE;
                }
            }
            firstChar = false;
            if (mode == KEY_DONE) {
                keyLength = offset;
                mode = NONE;
            }
            buf[offset++] = nextChar;
        }
        if (mode == UNICODE && count <= 4) {
            // luni.08=Invalid Unicode sequence: expected format \\uxxxx
            throw new IllegalArgumentException("luni.08");
        }
        if (keyLength == -1 && offset > 0) {
            keyLength = offset;
        }
        if (keyLength >= 0) {
            String temp = new String(buf, 0, offset);
            ReportField key = Enum.valueOf(ReportField.class, temp.substring(0, keyLength));
            String value = temp.substring(keyLength);
            if (mode == SLASH) {
                value += "\u0000";
            }
            put(key, value);
        }
    }

    private Enumeration<ReportField> keys() {
        return Collections.enumeration(keySet());
    }

    /**
     * Saves the mappings in this {@code Properties} to the specified
     * {@code OutputStream}, putting the specified comment at the beginning. The
     * output from this method is suitable for being read by the
     * {@link #load(InputStream)} method.
     * 
     * @param out
     *            the {@code OutputStream} to write to.
     * @param comment
     *            the comment to add at the beginning.
     * @throws ClassCastException
     *             if the key or value of a mapping is not a String.
     * @deprecated This method ignores any {@code IOException} thrown while
     *             writing -- use {@link #store} instead for better exception
     *             handling.
     */
    @Deprecated
    public void save(OutputStream out, String comment) {
        try {
            store(out, comment);
        } catch (IOException e) {
        }
    }

    /**
     * Maps the specified key to the specified value. If the key already exists,
     * the old value is replaced. The key and value cannot be {@code null}.
     * 
     * @param name
     *            the key.
     * @param value
     *            the value.
     * @return the old value mapped to the key, or {@code null}.
     */
    public Object setProperty(ReportField key, String value) {
        return put(key, value);
    }

    private static String lineSeparator = "\n";

    /**
     * Stores the mappings in this Properties to the specified OutputStream,
     * putting the specified comment at the beginning. The output from this
     * method is suitable for being read by the load() method.
     * 
     * @param out
     *            the OutputStream
     * @param comment
     *            the comment
     * @throws IOException
     * 
     * @exception ClassCastException
     *                when the key or value of a mapping is not a String
     */
    public synchronized void store(OutputStream out, String comment) throws IOException {

        StringBuilder buffer = new StringBuilder(200);
        OutputStreamWriter writer = new OutputStreamWriter(out, "ISO8859_1"); //$NON-NLS-1$
        if (comment != null) {
            writer.write("#"); //$NON-NLS-1$
            writer.write(comment);
            writer.write(lineSeparator);
        }

        for (Map.Entry<ReportField, String> entry : entrySet()) {
            String key = entry.getKey().toString();
            dumpString(buffer, key, true);
            buffer.append('=');
            dumpString(buffer, entry.getValue(), false);
            buffer.append(lineSeparator);
            writer.write(buffer.toString());
            buffer.setLength(0);
        }
        writer.flush();
    }

    /**
     * Stores the mappings in this Properties to the specified OutputStream,
     * putting the specified comment at the beginning. The output from this
     * method is suitable for being read by the load() method.
     * 
     * @param writer
     *            the writer
     * @param comment
     *            the comment
     * @throws IOException
     *             if any I/O exception occurs
     * @since 1.6
     */
    public synchronized void store(Writer writer, String comment) throws IOException {
        StringBuilder buffer = new StringBuilder(200);
        if (comment != null) {
            writer.write("#"); //$NON-NLS-1$
            writer.write(comment);
            writer.write(lineSeparator);
        }

        for (Map.Entry<ReportField, String> entry : entrySet()) {
            String key = entry.getKey().toString();
            dumpString(buffer, key, true);
            buffer.append('=');
            dumpString(buffer, entry.getValue(), false);
            buffer.append(lineSeparator);
            writer.write(buffer.toString());
            buffer.setLength(0);
        }
        writer.flush();
    }

    /**
     * Loads the properties from an {@code InputStream} containing the
     * properties in XML form. The XML document must begin with (and conform to)
     * following DOCTYPE:
     * 
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
     * </pre>
     * 
     * Also the content of the XML data must satisfy the DTD but the xml is not
     * validated against it. The DTD is not loaded from the SYSTEM ID. After
     * this method returns the InputStream is not closed.
     * 
     * @param in
     *            the InputStream containing the XML document.
     * @throws IOException
     *             in case an error occurs during a read operation.
     * @throws InvalidPropertiesFormatException
     *             if the XML data is not a valid properties file.
     */
    // public synchronized void loadFromXML(InputStream in) throws IOException,
    // InvalidPropertiesFormatException {
    // if (in == null) {
    // throw new NullPointerException();
    // }
    //
    // if (builder == null) {
    // DocumentBuilderFactory factory = DocumentBuilderFactory
    // .newInstance();
    // factory.setValidating(true);
    //
    // try {
    // builder = factory.newDocumentBuilder();
    // } catch (ParserConfigurationException e) {
    // throw new Error(e);
    // }
    //
    // builder.setErrorHandler(new ErrorHandler() {
    // public void warning(SAXParseException e) throws SAXException {
    // throw e;
    // }
    //
    // public void error(SAXParseException e) throws SAXException {
    // throw e;
    // }
    //
    // public void fatalError(SAXParseException e) throws SAXException {
    // throw e;
    // }
    // });
    //
    // builder.setEntityResolver(new EntityResolver() {
    // public InputSource resolveEntity(String publicId,
    // String systemId) throws SAXException, IOException {
    // if (systemId.equals(PROP_DTD_NAME)) {
    // InputSource result = new InputSource(new StringReader(
    // PROP_DTD));
    // result.setSystemId(PROP_DTD_NAME);
    // return result;
    // }
    // throw new SAXException("Invalid DOCTYPE declaration: "
    // + systemId);
    // }
    // });
    // }
    //
    // try {
    // Document doc = builder.parse(in);
    // NodeList entries = doc.getElementsByTagName("entry");
    // if (entries == null) {
    // return;
    // }
    // int entriesListLength = entries.getLength();
    //
    // for (int i = 0; i < entriesListLength; i++) {
    // Element entry = (Element) entries.item(i);
    // String key = entry.getAttribute("key");
    // String value = entry.getTextContent();
    //
    // /*
    // * key != null & value != null but key or(and) value can be
    // * empty String
    // */
    // put(key, value);
    // }
    // } catch (IOException e) {
    // throw e;
    // } catch (SAXException e) {
    // throw new InvalidPropertiesFormatException(e);
    // }
    // }

    /**
     * Writes all properties stored in this instance into the
     * {@code OutputStream} in XML representation. The DOCTYPE is
     * 
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
     * </pre>
     * 
     * If the comment is null, no comment is added to the output. UTF-8 is used
     * as the encoding. The {@code OutputStream} is not closed at the end. A
     * call to this method is the same as a call to {@code storeToXML(os,
     * comment, "UTF-8")}.
     * 
     * @param os
     *            the {@code OutputStream} to write to.
     * @param comment
     *            the comment to add. If null, no comment is added.
     * @throws IOException
     *             if an error occurs during writing to the output.
     */
    public void storeToXML(OutputStream os, String comment) throws IOException {
        storeToXML(os, comment, "UTF-8"); //$NON-NLS-1$
    }

    /**
     * Writes all properties stored in this instance into the
     * {@code OutputStream} in XML representation. The DOCTYPE is
     * 
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
     * </pre>
     * 
     * If the comment is null, no comment is added to the output. The parameter
     * {@code encoding} defines which encoding should be used. The
     * {@code OutputStream} is not closed at the end.
     * 
     * @param os
     *            the {@code OutputStream} to write to.
     * @param comment
     *            the comment to add. If null, no comment is added.
     * @param encoding
     *            the code identifying the encoding that should be used to write
     *            into the {@code OutputStream}.
     * @throws IOException
     *             if an error occurs during writing to the output.
     */
    public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {

        if (os == null || encoding == null) {
            throw new NullPointerException();
        }

        /*
         * We can write to XML file using encoding parameter but note that some
         * aliases for encodings are not supported by the XML parser. Thus we
         * have to know canonical name for encoding used to store data in XML
         * since the XML parser must recognize encoding name used to store data.
         */

        String encodingCanonicalName;
        try {
            encodingCanonicalName = Charset.forName(encoding).name();
        } catch (IllegalCharsetNameException e) {
            System.out.println("Warning: encoding name " + encoding + " is illegal, using UTF-8 as default encoding");
            encodingCanonicalName = "UTF-8";
        } catch (UnsupportedCharsetException e) {
            System.out.println("Warning: encoding " + encoding + " is not supported, using UTF-8 as default encoding");
            encodingCanonicalName = "UTF-8";
        }

        PrintStream printStream = new PrintStream(os, false, encodingCanonicalName);

        printStream.print("<?xml version=\"1.0\" encoding=\"");
        printStream.print(encodingCanonicalName);
        printStream.println("\"?>");

        printStream.print("<!DOCTYPE properties SYSTEM \"");
        printStream.print(PROP_DTD_NAME);
        printStream.println("\">");

        printStream.println("<properties>");

        if (comment != null) {
            printStream.print("<comment>");
            printStream.print(substitutePredefinedEntries(comment));
            printStream.println("</comment>");
        }

        for (Map.Entry<ReportField, String> entry : entrySet()) {
            String keyValue = entry.getKey().toString();
            String entryValue = entry.getValue();
            printStream.print("<entry key=\"");
            printStream.print(substitutePredefinedEntries(keyValue));
            printStream.print("\">");
            printStream.print(substitutePredefinedEntries(entryValue));
            printStream.println("</entry>");
        }
        printStream.println("</properties>");
        printStream.flush();
    }

    private String substitutePredefinedEntries(String s) {

        /*
         * substitution for predefined character entities to use them safely in
         * XML
         */
        return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                .replaceAll("\u0027", "&apos;").replaceAll("\"", "&quot;");
    }
}
