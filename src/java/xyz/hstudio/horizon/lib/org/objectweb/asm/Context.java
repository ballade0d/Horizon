package xyz.hstudio.horizon.lib.org.objectweb.asm;

/**
 * Information about a class being parsed in a {@link ClassReader}.
 *
 * @author Eric Bruneton
 */
class Context {

    /**
     * Prototypes of the attributes that must be parsed for this class.
     */
    Attribute[] attrs;

    /**
     * The {@link ClassReader} option flags for the parsing of this class.
     */
    int flags;

    /**
     * The buffer used to read strings.
     */
    char[] buffer;

    /**
     * The start index of each bootstrap method.
     */
    int[] bootstrapMethods;

    /**
     * The access flags of the method currently being parsed.
     */
    int access;

    /**
     * The name of the method currently being parsed.
     */
    String name;

    /**
     * The descriptor of the method currently being parsed.
     */
    String desc;

    /**
     * The label objects, indexed by bytecode offset, of the method currently
     * being parsed (only bytecode offsets for which a label is needed have a
     * non null associated Label object).
     */
    Label[] labels;

    /**
     * The target of the type annotation currently being parsed.
     */
    int typeRef;

    /**
     * The path of the type annotation currently being parsed.
     */
    TypePath typePath;

    /**
     * The offset of the latest stack map frame that has been parsed.
     */
    int offset;

    /**
     * The labels corresponding to the start of the local variable ranges in the
     * local variable type annotation currently being parsed.
     */
    Label[] start;

    /**
     * The labels corresponding to the end of the local variable ranges in the
     * local variable type annotation currently being parsed.
     */
    Label[] end;

    /**
     * The local variable indices for each local variable range in the local
     * variable type annotation currently being parsed.
     */
    int[] index;

    /**
     * The encoding of the latest stack map frame that has been parsed.
     */
    int mode;

    /**
     * The number of locals in the latest stack map frame that has been parsed.
     */
    int localCount;

    /**
     * The number locals in the latest stack map frame that has been parsed,
     * minus the number of locals in the previous frame.
     */
    int localDiff;

    /**
     * The local values of the latest stack map frame that has been parsed.
     */
    Object[] local;

    /**
     * The stack size of the latest stack map frame that has been parsed.
     */
    int stackCount;

    /**
     * The stack values of the latest stack map frame that has been parsed.
     */
    Object[] stack;
}