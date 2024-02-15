package se.openresult.unwrapeither;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes("se.openresult.unwrapeither.Unwrapped")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class UnwrappedProcessor extends AbstractProcessor {

    private static final String IO_JBOCK_UTIL_EITHER = "io.jbock.util.Either";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement typeElement = (TypeElement) element;
                    String annotatedClassName = typeElement.getQualifiedName().toString();

                    // Extracting value from the annotation
                    var unwrappedAnnotationString = typeElement.getAnnotation(Unwrapped.class).toString();
                    String leftClass = unwrappedAnnotationString.substring(
                                    unwrappedAnnotationString.indexOf("(") + 1,
                                    unwrappedAnnotationString.indexOf(".class)"));
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "leftClass: " + leftClass);

                    var fs = new ArrayList<UnwrapFunction>();
                    // Extracting method signatures
                    for (Element enclosedElement : typeElement.getEnclosedElements()) {
                        if (enclosedElement.getKind() == ElementKind.METHOD) {
                            ExecutableElement methodElement = (ExecutableElement) enclosedElement;
                            String methodName = methodElement.getSimpleName().toString();
                            var parameters = methodElement.getParameters().stream()
                                    .map(e -> new UnwrapParameter(e.getSimpleName().toString(), e.asType().toString()))
                                    .toList();
                            String returnType = methodElement.getReturnType().toString();
                            if (returnType.startsWith(IO_JBOCK_UTIL_EITHER)) {
                                var genericTypes = returnType
                                        .substring(IO_JBOCK_UTIL_EITHER.length() + 1, returnType.length() - 1)
                                        .split(",");
                                var left = genericTypes[0];
                                var right = genericTypes[1];
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                        String.format("%s.%s(%s): %s<%s,%s>", annotatedClassName, methodName,
                                                parameters, IO_JBOCK_UTIL_EITHER, left, right));
                                var f = new UnwrapFunction(left, right, methodName, parameters);
                                fs.add(f);
                            }
                        }
                    }
                    try {
                        writeUnwrappedFile(leftClass, annotatedClassName, fs);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
        }
        return true;
    }

    private void writeUnwrappedFile(String leftClass, String className, List<UnwrapFunction> fs) throws IOException {

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String wrapperClassName = className + "Unwrapped";
        String wrappedSimpleClassName = wrapperClassName.substring(lastDot + 1);
        String wrappedExceptionClassName = wrapperClassName + "Exception";
        String wrappedSimpleExceptionClassName = wrappedExceptionClassName.substring(lastDot + 1);

        writeUnwrappedExceptionFile(leftClass, packageName, wrappedExceptionClassName, wrappedSimpleExceptionClassName);

        JavaFileObject wrapperFile = processingEnv.getFiler().createSourceFile(wrapperClassName);
        try (PrintWriter out = new PrintWriter(wrapperFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }
            out.println("import java.util.function.Function;");
            out.println("import io.jbock.util.Either;");
            out.println();

            out.print("public abstract class ");
            out.print(wrappedSimpleClassName);
            out.println("<T, R> implements Function<T, R> {");
            out.println();

            out.print("    private final ");
            out.print(simpleClassName);
            out.println(" object;");
            out.println();

            out.print("    public ");
            out.print(wrappedSimpleClassName);
            out.print("(");
            out.print(simpleClassName);
            out.print(" object");
            out.println(") {");
            out.println("        this.object = object;");
            out.println("    }");
            out.println();

            for (var f : fs) {
                out.print("    public ");
                out.print(f.returnType());
                out.print(" ");
                out.print(f.functionName());
                out.println("(");
                var parameters = f.parameters().stream().map(p -> "            " + p.toCode()).collect(Collectors.joining(",\n"));
                out.print(parameters);
                out.println(") {");
                out.println("        return object");
                out.print("            .");
                out.print(f.functionName());
                out.print("(");
                var args = f.parameters().stream().map(p -> p.type()).collect(Collectors.joining(",\n"));
                out.print(args);
                out.println(")");
                out.print("            ");
                out.print(".orElseThrow(");
                out.print(wrappedSimpleExceptionClassName);
                out.println("::new);");
                out.println("    }");
                out.println();
            }

            out.println();
            out.println(String.format("    public Either<%s, R> execute(T arg) {", leftClass));
            out.println("        try {");
            out.println("            return Either.right(this.apply(arg));");
            out.println(String.format("        } catch (%s e) {", wrappedSimpleExceptionClassName));
            out.println("            return Either.left(e.left);");
            out.println("        }");
            out.println("    }");
            out.println("}");

        }
    }

    private void writeUnwrappedExceptionFile(
            String leftClass,
            String packageName,
            String wrappedExceptionClassName,
            String wrappedSimpleExceptionClassName) throws IOException {

        JavaFileObject wrapperFile = processingEnv.getFiler().createSourceFile(wrappedExceptionClassName);
        try (PrintWriter out = new PrintWriter(wrapperFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.print("public class ");
            out.print(wrappedSimpleExceptionClassName);
            out.println(" extends RuntimeException {");
            out.print("    public final ");
            out.print(leftClass);
            out.println(" left;");
            out.print("    public ");
            out.print(wrappedSimpleExceptionClassName);
            out.print("(");
            out.print(leftClass);
            out.println(" left) {");
            out.println("        this.left = left;");
            out.println("    }");
            out.println("}");
            out.println();
        }
    }

}
