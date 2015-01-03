package me.gotter.php;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple PHP-format unserializer
 */
public class Serialization
{
    /**
     * Recursive stateless parting
     *
     * @param buffer Buffer with data
     *
     * @return Object (null, Boolean, Long, Double, Map)
     * @throws SerializationException
     */
    static Object recursive(CharBuffer buffer) throws SerializationException
    {
        char current = buffer.current();

        if (current == 'N') {
            // Null sequence
            buffer.next();
            buffer.ensure(';');
            return null;
        }
        if (current == 'b') {
            // Boolean
            buffer.next();
            buffer.ensure(':');
            char type = buffer.next();
            buffer.next();
            buffer.ensure(';');

            if (type == '1') return true;
            else if (type == '0') return false;
            else throw new SerializationException("Unknown boolean flag %s at %s", type, buffer.getOffset() - 2);
        }
        if (current == 'i') {
            // Integer
            buffer.next();
            buffer.ensure(':');
            buffer.next();

            char[] intSig = buffer.readForward();
            buffer.ensure(';');

            return Long.parseLong(new String(intSig));
        }
        if (current == 'd') {
            // Double
            buffer.next();
            buffer.ensure(':');
            buffer.next();

            char[] doubleSig = buffer.readForward();
            buffer.ensure(';');


            return Double.parseDouble(new String(doubleSig));
        }
        if (current == 's') {
            // String
            buffer.next();
            buffer.ensure(':');
            buffer.next();

            char[] lengthSig = buffer.readForward();
            int length = Integer.parseInt(new String(lengthSig));
            buffer.next();
            buffer.next();

            char[] stringSig = buffer.read(length);
            buffer.next();
            buffer.ensure(';');

            return new String(stringSig);
        }
        if (current == 'a') {
            // Array (HashMap)
            buffer.next();
            buffer.ensure(':');
            buffer.next();
            char[] lengthSig = buffer.readForward();
            int length = Integer.parseInt(new String(lengthSig));
            buffer.ensure(':');

            if (length == 0) {
                // Zero-length array
                buffer.next();
                buffer.ensure('{');
                buffer.next();
                buffer.ensure('}');

                return Collections.EMPTY_MAP;
            }

            buffer.next();
            buffer.ensure('{');
            buffer.next();

            Map<String, Object> data = new LinkedHashMap<String, Object>(length);
            while (true) {
                String key = recursive(buffer).toString();
                buffer.next();
                Object value = recursive(buffer);
                data.put(key, value);

                buffer.next();
                if (buffer.current() == '}') break;


            }

            return data;
        }

        if (current == 'O') {
            throw new SerializationException("Object unserialization not supported");
        }

        throw new SerializationException("Unknown literal \"%s\" at %s", current, buffer.getOffset());
    }

    /**
     * Parses incoming string and returns object
     *
     * @param data Data to parse
     * @return Object (null, Boolean, Long, Double, Map)
     * @throws SerializationException
     */
    public Object parse(String data) throws SerializationException
    {
        if (data == null) throw new NullPointerException("Empty data provided to parse method");

        return recursive(new CharBuffer(data));
    }

    /**
     * Exception, thrown on parsing errors
     */
    public static class SerializationException extends Exception
    {
        public SerializationException(String message) {
            super(message);
        }

        public SerializationException(String message, Object... arg) {
            super(String.format(message, arg));
        }
    }

    /**
     * Utility class for char array traversal
     */
    static class CharBuffer
    {
        /**
         * Char array with data
         */
        final char[] buffer;

        /**
         * Char array length
         */
        final int length;

        /**
         * Current position in char array
         */
        int current;

        /**
         * Constructor
         *
         * @param data Source string
         */
        CharBuffer(String data) {
            buffer = data.toCharArray();
            length = buffer.length;
            current = 0;
        }

        /**
         * @return Current offset index
         */
        public int getOffset()
        {
            return current;
        }

        /**
         * @return Char at current offset
         */
        public char current()
        {
            if (current >= length) throw new RuntimeException("Out of range");
            return buffer[current];
        }

        /**
         * Asserts, that {@see current} points to one of provided values
         *
         * @param to Assertion
         * @throws SerializationException
         */
        public void ensure(char... to) throws SerializationException {
            if (current >= length) throw new SerializationException(
                "Read after end of array while ensure"
            );

            for (char aTo : to) {
                if (aTo == buffer[current]) return;
            }

            throw new SerializationException(
                "Unable to ensure %s = %s at position %s.", buffer[current], to, current);
        }

        /**
         * Reads all data until separator
         *
         * @return Data read
         */
        public char[] readForward()
        {
            int stop = 0;
            for (int i=current; i < length; i++) {
                if (buffer[i] == ':' || buffer[i] == ';') {
                    stop = i;
                    break;
                }
            }
            if (stop == 0) return new char[0];
            char[] response = new char[stop - current];
            System.arraycopy(buffer, current, response, 0, response.length);
            current += response.length;
            return response;
        }

        /**
         * Reads amount of chars
         *
         * @param length Amount of data to read
         * @return Data read
         */
        public char[] read(int length)
        {
            char[] response = new char[length];
            System.arraycopy(buffer, current, response, 0, length);
            current += length;
            return response;
        }

        /**
         * Forwards pointer to next position
         *
         * @return char at next position
         */
        public char next()
        {
            current++;
            return current();
        }
    }
}
