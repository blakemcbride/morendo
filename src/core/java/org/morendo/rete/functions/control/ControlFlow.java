package org.morendo.rete.functions.control;

/**
 * Thrown by (break) and (return) to unwind to the enclosing loop, deffunction body or rule action
 * list. It carries no stack trace: it is control flow, not an error.
 */
public final class ControlFlow extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** What the statement asks for. */
    public enum Kind {
        BREAK,
        RETURN
    }

    private final Kind kind;
    private final transient Object value;

    public ControlFlow(Kind kind, Object value) {
        super(kind.name(), null, false, false);
        this.kind = kind;
        this.value = value;
    }

    public Kind getKind() {
        return this.kind;
    }

    /** The value given to (return), or null. */
    public Object getValue() {
        return this.value;
    }

    public boolean isReturn() {
        return this.kind == Kind.RETURN;
    }
}
