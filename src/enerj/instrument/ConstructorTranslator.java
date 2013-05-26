package enerj.instrument;

import checkers.runtime.instrument.HelpfulTreeTranslator;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.TreeUtils;

import javax.annotation.processing.ProcessingEnvironment;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import enerj.PrecisionChecker;
import enerj.lang.Approx;

public class ConstructorTranslator
        extends HelpfulTreeTranslator<PrecisionChecker> {
    protected static String CONSTRUCTOR_MARKER_CLASS =
        ConstructorMarker.class.getName();

    public ConstructorTranslator(PrecisionChecker checker,
                                 ProcessingEnvironment env,
                                 TreePath p) {
        super(checker, env, p);
    }

    // Add a new, uninstrumented constructor.
    @Override
    public void visitClassDef(JCTree.JCClassDecl cls) {
        super.visitClassDef(cls);

        // Check for the kinds of classes that can't have methods.
        if ((cls.sym.flags_field & (Flags.INTERFACE | Flags.ENUM)) != 0) {
            return;
        }

        // Get the Type of the CONSTRUCTOR_MARKER_CLASS.
        JCTree.JCExpression instExp =
            dotsExp(CONSTRUCTOR_MARKER_CLASS + ".inst");
        Type markerType = typeForExpr(instExp,
                                      enter.getClassEnv(cls.sym));

        Symbol.MethodSymbol constructorSym = new Symbol.MethodSymbol(
            Flags.PUBLIC,
            names.fromString("<init>"),
            new Type.MethodType(
                List.<Type>of( // Parameters.
                    markerType
                ),
                (Type)symtab.voidType, // Return type.
                List.<Type>nil(),
                (Symbol.TypeSymbol)null
            ),
            cls.sym
        );
        JCTree.JCMethodDecl constructor = maker.MethodDef(
            constructorSym,
            maker.Block(
                0,
                List.<JCTree.JCStatement>nil()
            )
        );

        enterClassMember(cls, constructor);
        attr.attribStat(constructor, enter.getClassEnv(cls.sym));
        cls.defs = cls.defs.append(constructor);
    }
}
