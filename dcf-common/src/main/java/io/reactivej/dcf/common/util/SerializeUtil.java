package io.reactivej.dcf.common.util;

import org.apache.commons.lang3.SerializationException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class SerializeUtil {

    public static <T> T deserialize(byte[] data, ClassLoader classLoader) {
        try {
            return deserialize(new ClassLoaderAwareObjectInputStream(new ByteArrayInputStream(data), classLoader));
        } catch (final IOException ex) {
            throw new SerializationException(ex);
        }
    }

    public static <T> T deserialize(final ObjectInputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        try {
            // stream closed in the finally
            @SuppressWarnings("unchecked") // may fail with CCE if serialised form is incorrect
            final T obj = (T) inputStream.readObject();
            return obj;

        } catch (final ClassCastException ex) {
            throw new SerializationException(ex);
        } catch (final ClassNotFoundException ex) {
            throw new SerializationException(ex);
        } catch (final IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (final IOException ex) { // NOPMD
                // ignore close exception
            }
        }
    }

    static class ClassLoaderAwareObjectInputStream extends ObjectInputStream {
        private static final Map<String, Class<?>> primitiveTypes =
                new HashMap<String, Class<?>>();
        private final ClassLoader classLoader;

        /**
         * Constructor.
         * @param in The <code>InputStream</code>.
         * @param classLoader classloader to use
         * @throws IOException if an I/O error occurs while reading stream header.
         * @see java.io.ObjectInputStream
         */
        public ClassLoaderAwareObjectInputStream(final InputStream in, final ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;

            primitiveTypes.put("byte", byte.class);
            primitiveTypes.put("short", short.class);
            primitiveTypes.put("int", int.class);
            primitiveTypes.put("long", long.class);
            primitiveTypes.put("float", float.class);
            primitiveTypes.put("double", double.class);
            primitiveTypes.put("boolean", boolean.class);
            primitiveTypes.put("char", char.class);
            primitiveTypes.put("void", void.class);
        }

        /**
         * Overriden version that uses the parametrized <code>ClassLoader</code> or the <code>ClassLoader</code>
         * of the current <code>Thread</code> to resolve the class.
         * @param desc An instance of class <code>ObjectStreamClass</code>.
         * @return A <code>Class</code> object corresponding to <code>desc</code>.
         * @throws IOException Any of the usual Input/Output exceptions.
         * @throws ClassNotFoundException If class of a serialized object cannot be found.
         */
        @Override
        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            final String name = desc.getName();
            try {
                return Class.forName(name, false, classLoader);
            } catch (final ClassNotFoundException ex) {
                try {
                    return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
                } catch (final ClassNotFoundException cnfe) {
                    final Class<?> cls = primitiveTypes.get(name);
                    if (cls != null) {
                        return cls;
                    } else {
                        throw cnfe;
                    }
                }
            }
        }

    }
}
