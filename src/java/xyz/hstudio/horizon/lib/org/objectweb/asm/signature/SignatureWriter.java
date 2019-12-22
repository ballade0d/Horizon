package xyz.hstudio.horizon.lib.org.objectweb.asm.signature;

import xyz.hstudio.horizon.lib.org.objectweb.asm.Opcodes;

/**
 * A signature visitor that generates signatures in string format.
 *
 * @author Thomas Hallgren
 * @author Eric Bruneton
 */
public class SignatureWriter extends SignatureVisitor {

    /**
     * Builder used to construct the signature.
     */
    private final StringBuilder buf = new StringBuilder();

    /**
     * Indicates if the signature contains formal type parameters.
     */
    private boolean hasFormals;

    /**
     * Indicates if the signature contains method parameter types.
     */
    private boolean hasParameters;

    /**
     * Stack used to keep track of class types that have arguments. Each element
     * of this stack is a boolean encoded in one bit. The top of the stack is
     * the lowest order bit. Pushing false = *2, pushing true = *2+1, popping =
     * /2.
     */
    private int argumentStack;

    /**
     * Constructs a new {@link SignatureWriter} object.
     */
    public SignatureWriter() {
        super(Opcodes.ASM5);
    }

    // ------------------------------------------------------------------------
    // Implementation of the SignatureVisitor interface
    // ------------------------------------------------------------------------

    @Override
    public void visitFormalTypeParameter(final String name) {
        if (!hasFormals) {
            hasFormals = true;
            buf.append('<');
        }
        buf.append(name);
        buf.append(':');
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        buf.append(':');
        return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        endFormals();
        return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
        return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        endFormals();
        if (!hasParameters) {
            hasParameters = true;
            buf.append('(');
        }
        return this;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        endFormals();
        if (!hasParameters) {
            buf.append('(');
        }
        buf.append(')');
        return this;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        buf.append('^');
        return this;
    }

    @Override
    public void visitBaseType(final char descriptor) {
        buf.append(descriptor);
    }

    @Override
    public void visitTypeVariable(final String name) {
        buf.append('T');
        buf.append(name);
        buf.append(';');
    }

    @Override
    public SignatureVisitor visitArrayType() {
        buf.append('[');
        return this;
    }

    @Override
    public void visitClassType(final String name) {
        buf.append('L');
        buf.append(name);
        argumentStack *= 2;
    }

    @Override
    public void visitInnerClassType(final String name) {
        endArguments();
        buf.append('.');
        buf.append(name);
        argumentStack *= 2;
    }

    @Override
    public void visitTypeArgument() {
        if (argumentStack % 2 == 0) {
            ++argumentStack;
            buf.append('<');
        }
        buf.append('*');
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        if (argumentStack % 2 == 0) {
            ++argumentStack;
            buf.append('<');
        }
        if (wildcard != '=') {
            buf.append(wildcard);
        }
        return this;
    }

    @Override
    public void visitEnd() {
        endArguments();
        buf.append(';');
    }

    /**
     * Returns the signature that was built by this signature writer.
     *
     * @return the signature that was built by this signature writer.
     */
    @Override
    public String toString() {
        return buf.toString();
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    /**
     * Ends the formal type parameters section of the signature.
     */
    private void endFormals() {
        if (hasFormals) {
            hasFormals = false;
            buf.append('>');
        }
    }

    /**
     * Ends the type arguments of a class or inner class type.
     */
    private void endArguments() {
        if (argumentStack % 2 != 0) {
            buf.append('>');
        }
        argumentStack /= 2;
    }
}