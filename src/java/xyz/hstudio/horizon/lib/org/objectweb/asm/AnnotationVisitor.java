package xyz.hstudio.horizon.lib.org.objectweb.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A visitor to visit a Java annotation. The methods of this class must be
 * called in the following order: ( <tt>visit</tt> | <tt>visitEnum</tt> |
 * <tt>visitAnnotation</tt> | <tt>visitArray</tt> )* <tt>visitEnd</tt>.
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public abstract class AnnotationVisitor {

    /**
     * The ASM API version implemented by this visitor. The value of this field
     * must be one of {@link org.objectweb.asm.Opcodes#ASM4} or {@link org.objectweb.asm.Opcodes#ASM5}.
     */
    protected final int api;

    /**
     * The annotation visitor to which this visitor must delegate method calls.
     * May be null.
     */
    protected AnnotationVisitor av;

    /**
     * Constructs a new {@link AnnotationVisitor}.
     *
     * @param api the ASM API version implemented by this visitor. Must be one
     *            of {@link org.objectweb.asm.Opcodes#ASM4} or {@link org.objectweb.asm.Opcodes#ASM5}.
     */
    public AnnotationVisitor(final int api) {
        this(api, null);
    }

    /**
     * Constructs a new {@link AnnotationVisitor}.
     *
     * @param api the ASM API version implemented by this visitor. Must be one
     *            of {@link org.objectweb.asm.Opcodes#ASM4} or {@link org.objectweb.asm.Opcodes#ASM5}.
     * @param av  the annotation visitor to which this visitor must delegate
     *            method calls. May be null.
     */
    public AnnotationVisitor(final int api, final AnnotationVisitor av) {
        if (api != org.objectweb.asm.Opcodes.ASM4 && api != Opcodes.ASM5) {
            throw new IllegalArgumentException();
        }
        this.api = api;
        this.av = av;
    }

    /**
     * Visits a primitive value of the annotation.
     *
     * @param name  the value name.
     * @param value the actual value, whose type must be {@link Byte},
     *              {@link Boolean}, {@link Character}, {@link Short},
     *              {@link Integer} , {@link Long}, {@link Float}, {@link Double},
     *              {@link String} or {@link Type} or OBJECT or ARRAY sort. This
     *              value can also be an array of byte, boolean, short, char, int,
     *              long, float or double values (this is equivalent to using
     *              {@link #visitArray visitArray} and visiting each array element
     *              in turn, but is more convenient).
     */
    public void visit(String name, Object value) {
        if (av != null) {
            av.visit(name, value);
        }
    }

    /**
     * Visits an enumeration value of the annotation.
     *
     * @param name  the value name.
     * @param desc  the class descriptor of the enumeration class.
     * @param value the actual enumeration value.
     */
    public void visitEnum(String name, String desc, String value) {
        if (av != null) {
            av.visitEnum(name, desc, value);
        }
    }

    /**
     * Visits a nested annotation value of the annotation.
     *
     * @param name the value name.
     * @param desc the class descriptor of the nested annotation class.
     * @return a visitor to visit the actual nested annotation value, or
     * <tt>null</tt> if this visitor is not interested in visiting this
     * nested annotation. <i>The nested annotation value must be fully
     * visited before calling other methods on this annotation
     * visitor</i>.
     */
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        if (av != null) {
            return av.visitAnnotation(name, desc);
        }
        return null;
    }

    /**
     * Visits an array value of the annotation. Note that arrays of primitive
     * types (such as byte, boolean, short, char, int, long, float or double)
     * can be passed as value to {@link #visit visit}. This is what
     * {@link ClassReader} does.
     *
     * @param name the value name.
     * @return a visitor to visit the actual array value elements, or
     * <tt>null</tt> if this visitor is not interested in visiting these
     * values. The 'name' parameters passed to the methods of this
     * visitor are ignored. <i>All the array values must be visited
     * before calling other methods on this annotation visitor</i>.
     */
    public AnnotationVisitor visitArray(String name) {
        if (av != null) {
            return av.visitArray(name);
        }
        return null;
    }

    /**
     * Visits the end of the annotation.
     */
    public void visitEnd() {
        if (av != null) {
            av.visitEnd();
        }
    }
}